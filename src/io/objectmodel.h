#pragma once

#include <QList>
#include <QString>
#include <QStringList>
#include <QVariant>

#include <memory>
#include <stdexcept>
#include <vector>

namespace jbead {

class FileFormatException : public std::runtime_error
{
public:
    explicit FileFormatException(const QString& msg)
        : std::runtime_error(msg.toStdString()) {}
};

/*  In-memory tree for the .jbb S-expression format. Faithful port of
    legacy ch.jbead.storage. The tree shape is:

      Node(name)        — non-leaf, holds child Nodes
      Leaf(name)        — terminal, holds 0..N values (int / bool /
                          QString)

    Path syntax: slash-separated names — "view/draft-visible"
    addresses the leaf "draft-visible" under the "view" child.

    Values are kept as QVariant carrying int, bool, or QString. The
    lexer converts true/false to bool and decimal integer literals
    to int; everything else (including dates — we don't need them)
    is stored as identifier QString.                              */

class Node
{
public:
    explicit Node(QString name) : m_name(std::move(name)) {}
    virtual ~Node() = default;

    QString name() const { return m_name; }
    virtual bool isLeaf() const { return false; }

    int   childCount() const { return int(m_children.size()); }
    Node* child(int i) const { return m_children.at(i).get(); }
    const std::vector<std::unique_ptr<Node>>& children() const { return m_children; }

    Node* find(const QString& name) const;
    QList<Node*> findAll(const QString& name) const;
    Node* findOrAdd(const QString& name);
    Node* addChild(std::unique_ptr<Node> n);

protected:
    QString m_name;
    std::vector<std::unique_ptr<Node>> m_children;
};

class Leaf : public Node
{
public:
    explicit Leaf(QString name) : Node(std::move(name)) {}
    Leaf(QString name, QList<QVariant> values) : Node(std::move(name)), m_values(std::move(values)) {}

    bool isLeaf() const override { return true; }

    int valueCount() const { return m_values.size(); }
    QVariant value(int idx = 0) const { return m_values.at(idx); }
    const QList<QVariant>& values() const { return m_values; }
    void appendValue(QVariant v) { m_values.append(std::move(v)); }

    int     intValue(int idx = 0) const;
    QString stringValue(int idx = 0) const;
    bool    boolValue(int idx = 0) const;

private:
    QList<QVariant> m_values;
};

class ObjectModel
{
public:
    explicit ObjectModel(const QString& rootName);

    /*  Add a leaf at the given slash-separated path. Trailing path
        segment names the leaf; leading segments are intermediate
        Nodes (created on demand via findOrAdd).                  */
    void add(const QString& pathStr, const QList<QVariant>& values);
    void add(const QString& pathStr, std::initializer_list<QVariant> values);

    Node* get(const QString& pathStr) const;
    QList<Node*> getAll(const QString& pathStr) const;

    int     intValue(const QString& path, int defaultValue) const;
    QString stringValue(const QString& path, const QString& defaultValue) const;
    bool    boolValue(const QString& path, bool defaultValue) const;

    /*  Pretty-print as an indented S-expression, matching the
        legacy format byte-for-byte (4-space indent, leaves on a
        single line).                                              */
    QString toString() const;

    /*  Parse from S-expression text. Throws FileFormatException on
        a syntax error.                                            */
    static ObjectModel fromData(const QString& data, const QString& rootName);

    Node* root() const { return m_root.get(); }

private:
    static QString formatNode(const Node* node, const QString& indent);
    std::unique_ptr<Node> m_root;
};

} // namespace jbead
