#include "objectmodel.h"

#include <QChar>

namespace jbead {

// ---- Node helpers ------------------------------------------------

Node* Node::find(const QString& name) const
{
    for (const auto& c : m_children) {
        if (c->name() == name) return c.get();
    }
    return nullptr;
}

QList<Node*> Node::findAll(const QString& name) const
{
    QList<Node*> out;
    for (const auto& c : m_children) {
        if (c->name() == name) out.append(c.get());
    }
    return out;
}

Node* Node::findOrAdd(const QString& name)
{
    if (Node* n = find(name)) return n;
    return addChild(std::make_unique<Node>(name));
}

Node* Node::addChild(std::unique_ptr<Node> n)
{
    Node* raw = n.get();
    m_children.push_back(std::move(n));
    return raw;
}

// ---- Leaf accessors ----------------------------------------------

int Leaf::intValue(int idx) const
{
    bool ok = false;
    const int v = m_values.at(idx).toInt(&ok);
    if (!ok) throw FileFormatException(
        QStringLiteral("Expected integer value but got %1").arg(m_values.at(idx).toString()));
    return v;
}

QString Leaf::stringValue(int idx) const
{
    return m_values.at(idx).toString();
}

bool Leaf::boolValue(int idx) const
{
    const QVariant& v = m_values.at(idx);
    if (v.typeId() == QMetaType::Bool) return v.toBool();
    throw FileFormatException(
        QStringLiteral("Expected boolean value but got %1").arg(v.toString()));
}

// ---- ObjectModel -------------------------------------------------

ObjectModel::ObjectModel(const QString& rootName)
    : m_root(std::make_unique<Node>(rootName))
{
}

void ObjectModel::add(const QString& pathStr, const QList<QVariant>& values)
{
    QStringList parts = pathStr.split(QLatin1Char('/'));
    const QString leafName = parts.takeLast();
    Node* current = m_root.get();
    for (const QString& seg : std::as_const(parts)) {
        current = current->findOrAdd(seg);
    }
    current->addChild(std::make_unique<Leaf>(leafName, values));
}

void ObjectModel::add(const QString& pathStr, std::initializer_list<QVariant> values)
{
    add(pathStr, QList<QVariant>(values.begin(), values.end()));
}

Node* ObjectModel::get(const QString& pathStr) const
{
    QStringList parts = pathStr.split(QLatin1Char('/'));
    Node* current = m_root.get();
    for (const QString& seg : std::as_const(parts)) {
        current = current->find(seg);
        if (!current) throw FileFormatException(
            QStringLiteral("Path %1 cannot be resolved, node %2 not found")
                .arg(pathStr, seg));
    }
    return current;
}

QList<Node*> ObjectModel::getAll(const QString& pathStr) const
{
    QStringList parts = pathStr.split(QLatin1Char('/'));
    const QString leafName = parts.takeLast();
    Node* current = m_root.get();
    for (const QString& seg : std::as_const(parts)) {
        current = current->find(seg);
        if (!current) throw FileFormatException(
            QStringLiteral("Path %1 cannot be resolved, node %2 not found")
                .arg(pathStr, seg));
    }
    return current->findAll(leafName);
}

int ObjectModel::intValue(const QString& path, int defaultValue) const
{
    try {
        Node* n = get(path);
        if (!n->isLeaf()) return defaultValue;
        return static_cast<Leaf*>(n)->intValue();
    } catch (const FileFormatException&) { return defaultValue; }
}

QString ObjectModel::stringValue(const QString& path, const QString& defaultValue) const
{
    try {
        Node* n = get(path);
        if (!n->isLeaf()) return defaultValue;
        return static_cast<Leaf*>(n)->stringValue();
    } catch (const FileFormatException&) { return defaultValue; }
}

bool ObjectModel::boolValue(const QString& path, bool defaultValue) const
{
    try {
        Node* n = get(path);
        if (!n->isLeaf()) return defaultValue;
        return static_cast<Leaf*>(n)->boolValue();
    } catch (const FileFormatException&) { return defaultValue; }
}

QString ObjectModel::formatNode(const Node* node, const QString& indent)
{
    if (node->isLeaf()) {
        const Leaf* leaf = static_cast<const Leaf*>(node);
        QString out;
        out += indent + QLatin1Char('(') + leaf->name();
        for (const QVariant& v : leaf->values()) {
            out += QLatin1Char(' ');
            switch (v.typeId()) {
                case QMetaType::QString: {
                    QString escaped = v.toString();
                    escaped.replace(QLatin1Char('\\'), QStringLiteral("\\\\"));
                    escaped.replace(QLatin1Char('"'), QStringLiteral("\\\""));
                    out += QLatin1Char('"') + escaped + QLatin1Char('"');
                    break;
                }
                case QMetaType::Bool:
                    out += v.toBool() ? QStringLiteral("true") : QStringLiteral("false");
                    break;
                default:
                    out += v.toString();
                    break;
            }
        }
        out += QStringLiteral(")\n");
        return out;
    }
    QString out = indent + QLatin1Char('(') + node->name() + QLatin1Char('\n');
    for (const auto& child : node->children()) {
        out += formatNode(child.get(), indent + QStringLiteral("    "));
    }
    if (out.endsWith(QLatin1Char('\n'))) out.chop(1);
    out += QStringLiteral(")\n");
    return out;
}

QString ObjectModel::toString() const
{
    return formatNode(m_root.get(), QString());
}

// ---- Lexer + parser ---------------------------------------------

namespace {

enum class LexerState { Start, QuotedString, QuotedStringEscape, GeneralToken, Comment };

class Tokens
{
public:
    explicit Tokens(const QString& data) { parse(data); }

    int  size() const { return m_tokens.size(); }
    const QVariant& at(int i) const { return m_tokens.at(i); }

private:
    QList<QVariant> m_tokens;

    void parse(const QString& data)
    {
        LexerState state = LexerState::Start;
        QString tok;
        for (QChar ch : data) {
            switch (state) {
                case LexerState::Start: startState(ch, tok, state); break;
                case LexerState::QuotedString: quotedStringState(ch, tok, state); break;
                case LexerState::QuotedStringEscape: quotedStringEscapeState(ch, tok, state); break;
                case LexerState::GeneralToken: generalTokenState(ch, tok, state); break;
                case LexerState::Comment: if (ch == QLatin1Char('\n')) state = LexerState::Start; break;
            }
        }
        if (!tok.isEmpty()) addToken(tok);
    }

    void startState(QChar ch, QString& tok, LexerState& state)
    {
        if (ch.isSpace()) return;
        if (ch == QLatin1Char('(') || ch == QLatin1Char(')')) {
            m_tokens.append(QString(ch));
            return;
        }
        if (ch == QLatin1Char('"')) {
            state = LexerState::QuotedString;
            return;
        }
        if (ch == QLatin1Char('#')) { state = LexerState::Comment; return; }
        tok.append(ch);
        state = LexerState::GeneralToken;
    }

    void quotedStringState(QChar ch, QString& tok, LexerState& state)
    {
        if (ch == QLatin1Char('\\')) { state = LexerState::QuotedStringEscape; return; }
        if (ch == QLatin1Char('"')) {
            /*  Stored *unquoted* — the parser identifies strings
                downstream via QVariant typeId == QString.         */
            m_tokens.append(QVariant(tok));
            tok.clear();
            state = LexerState::Start;
            return;
        }
        tok.append(ch);
    }

    void quotedStringEscapeState(QChar ch, QString& tok, LexerState& state)
    {
        tok.append(ch);
        state = LexerState::QuotedString;
    }

    void generalTokenState(QChar ch, QString& tok, LexerState& state)
    {
        if (ch == QLatin1Char(' ') || ch == QLatin1Char('\n') || ch == QLatin1Char('\r')) {
            addToken(tok); tok.clear(); state = LexerState::Start;
        } else if (ch == QLatin1Char('(') || ch == QLatin1Char(')')) {
            addToken(tok); tok.clear();
            m_tokens.append(QString(ch));
            state = LexerState::Start;
        } else {
            tok.append(ch);
        }
    }

    void addToken(const QString& tok)
    {
        if (tok.isEmpty()) return;
        if (tok == QStringLiteral("true"))  { m_tokens.append(true);  return; }
        if (tok == QStringLiteral("false")) { m_tokens.append(false); return; }
        bool ok = false;
        const int n = tok.toInt(&ok);
        if (ok) { m_tokens.append(n); return; }
        /*  Identifiers (including parenthesis sentinels) are stored
            as QString. The parser distinguishes structure tokens
            "(" / ")" from identifiers by exact string match.      */
        m_tokens.append(QVariant(tok));
    }
};

class Parser
{
public:
    explicit Parser(const Tokens& tokens) : m_tokens(tokens) {}

    std::unique_ptr<Node> parse()
    {
        return parseNode(next());
    }

private:
    const Tokens& m_tokens;
    int m_pos = 0;

    QVariant next()
    {
        if (m_pos >= m_tokens.size())
            throw FileFormatException(QStringLiteral("Syntax error, unexpected end of file"));
        return m_tokens.at(m_pos++);
    }

    static bool isOpenParen(const QVariant& v)
    {
        return v.typeId() == QMetaType::QString && v.toString() == QStringLiteral("(");
    }
    static bool isCloseParen(const QVariant& v)
    {
        return v.typeId() == QMetaType::QString && v.toString() == QStringLiteral(")");
    }

    std::unique_ptr<Node> parseNode(QVariant token)
    {
        if (!isOpenParen(token))
            throw FileFormatException(
                QStringLiteral("Syntax error, expected ( but got %1").arg(token.toString()));
        token = next();
        if (token.typeId() != QMetaType::QString)
            throw FileFormatException(QStringLiteral("Expected node name, got non-identifier token"));
        const QString name = token.toString();
        token = next();
        if (isCloseParen(token)) {
            return std::make_unique<Node>(name);
        }
        if (isOpenParen(token)) {
            auto node = std::make_unique<Node>(name);
            while (!isCloseParen(token)) {
                node->addChild(parseNode(token));
                token = next();
            }
            return node;
        }
        auto leaf = std::make_unique<Leaf>(name);
        while (!isCloseParen(token)) {
            leaf->appendValue(token);
            token = next();
        }
        return leaf;
    }
};

} // namespace

ObjectModel ObjectModel::fromData(const QString& data, const QString& /*rootName*/)
{
    Tokens tokens(data);
    Parser parser(tokens);
    auto node = parser.parse();
    /*  Wrap in an ObjectModel by stealing the parsed node into the
        m_root slot. The provided rootName is ignored because the
        outermost node already carries its own name from the
        source.                                                   */
    ObjectModel om(node->name());
    om.m_root = std::move(node);
    return om;
}

} // namespace jbead
