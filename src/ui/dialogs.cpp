#include "dialogs.h"

#include "beadpainter.h"
#include "domain/beadcounts.h"
#include "domain/beadsymbols.h"
#include "domain/model.h"

#include <QComboBox>
#include <QDialogButtonBox>
#include <QFileInfo>
#include <QFormLayout>
#include <QHeaderView>
#include <QLabel>
#include <QPainter>
#include <QPixmap>
#include <QSettings>
#include <QSpinBox>
#include <QTreeWidget>
#include <QTreeWidgetItem>
#include <QVBoxLayout>

namespace jbead {

ArrangeDialog::ArrangeDialog(int defaultCopies, int defaultOffset, QWidget* parent)
    : QDialog(parent)
{
    setWindowTitle(tr("Arrange"));
    auto* form = new QFormLayout;
    m_copies = new QSpinBox(this);
    m_copies->setRange(1, 1000);
    m_copies->setValue(defaultCopies);
    m_offset = new QSpinBox(this);
    m_offset->setRange(-100000, 100000);
    m_offset->setValue(defaultOffset);
    form->addRow(tr("&Copies:"), m_copies);
    form->addRow(tr("&Offset:"), m_offset);

    auto* btns = new QDialogButtonBox(QDialogButtonBox::Ok | QDialogButtonBox::Cancel, this);
    connect(btns, &QDialogButtonBox::accepted, this, &QDialog::accept);
    connect(btns, &QDialogButtonBox::rejected, this, &QDialog::reject);

    auto* layout = new QVBoxLayout(this);
    layout->addLayout(form);
    layout->addWidget(btns);
}

int ArrangeDialog::copies() const { return m_copies->value(); }
int ArrangeDialog::offset() const { return m_offset->value(); }

// -------------------------------------------------------------------

IntPromptDialog::IntPromptDialog(const QString& title, const QString& label,
                                 int currentValue, int minValue, int maxValue,
                                 QWidget* parent)
    : QDialog(parent)
{
    setWindowTitle(title);
    auto* form = new QFormLayout;
    m_value = new QSpinBox(this);
    m_value->setRange(minValue, maxValue);
    m_value->setValue(currentValue);
    form->addRow(label, m_value);

    auto* btns = new QDialogButtonBox(QDialogButtonBox::Ok | QDialogButtonBox::Cancel, this);
    connect(btns, &QDialogButtonBox::accepted, this, &QDialog::accept);
    connect(btns, &QDialogButtonBox::rejected, this, &QDialog::reject);

    auto* layout = new QVBoxLayout(this);
    layout->addLayout(form);
    layout->addWidget(btns);
}

int IntPromptDialog::value() const { return m_value->value(); }

// -------------------------------------------------------------------

PreferencesDialog::PreferencesDialog(QWidget* parent)
    : QDialog(parent)
{
    setWindowTitle(tr("Preferences"));

    QSettings s;
    const QString currentLang  = s.value(QStringLiteral("Environment/Language"),
                                         QStringLiteral("system")).toString();
    const QString currentTheme = s.value(QStringLiteral("Environment/ColorScheme"),
                                         QStringLiteral("system")).toString();

    auto* form = new QFormLayout;

    m_language = new QComboBox(this);
    m_language->addItem(tr("System default"), QStringLiteral("system"));
    m_language->addItem(QStringLiteral("English"), QStringLiteral("en"));
    m_language->addItem(QStringLiteral("Deutsch"), QStringLiteral("de"));
    m_language->addItem(QStringLiteral("Français"), QStringLiteral("fr"));
    m_language->setCurrentIndex(qMax(0, m_language->findData(currentLang)));
    form->addRow(tr("&Language:"), m_language);

    m_colorScheme = new QComboBox(this);
    m_colorScheme->addItem(tr("Follow system"), QStringLiteral("system"));
    m_colorScheme->addItem(tr("Light"), QStringLiteral("light"));
    m_colorScheme->addItem(tr("Dark"),  QStringLiteral("dark"));
    m_colorScheme->setCurrentIndex(qMax(0, m_colorScheme->findData(currentTheme)));
    form->addRow(tr("&Color scheme:"), m_colorScheme);

    auto* hint = new QLabel(tr("Language changes take effect after restarting JBead."), this);
    QFont f = hint->font(); f.setItalic(true); hint->setFont(f);

    auto* btns = new QDialogButtonBox(QDialogButtonBox::Ok | QDialogButtonBox::Cancel, this);
    connect(btns, &QDialogButtonBox::accepted, this, &QDialog::accept);
    connect(btns, &QDialogButtonBox::rejected, this, &QDialog::reject);

    auto* layout = new QVBoxLayout(this);
    layout->addLayout(form);
    layout->addWidget(hint);
    layout->addWidget(btns);
}

QString PreferencesDialog::language()    const { return m_language->currentData().toString(); }
QString PreferencesDialog::colorScheme() const { return m_colorScheme->currentData().toString(); }

// -------------------------------------------------------------------

namespace {

QPixmap colorSwatch(const QColor& color, int size = 18)
{
    QPixmap pm(size, size);
    pm.fill(Qt::transparent);
    QPainter p(&pm);
    p.fillRect(1, 1, size - 2, size - 2, color);
    p.setPen(Qt::black);
    p.drawRect(1, 1, size - 2, size - 2);
    return pm;
}

} // namespace

TechInfosDialog::TechInfosDialog(const Model& model, QWidget* parent)
    : QDialog(parent)
{
    setWindowTitle(tr("Technical Information"));

    /*  Top form: file metadata + high-level counts. updateRepeat()
        runs eagerly so the displayed repeat count matches the
        model state at dialog open time, avoiding a stale value
        that's only refreshed by the report panel's paintEvent.   */
    if (model.isRepeatDirty()) const_cast<Model&>(model).updateRepeat();

    auto* form = new QFormLayout;
    auto addRow = [&](const QString& label, const QString& value) {
        auto* lbl = new QLabel(value, this);
        lbl->setTextInteractionFlags(Qt::TextSelectableByMouse);
        form->addRow(label, lbl);
    };
    QString file = QFileInfo(model.filePath()).fileName();
    if (file.isEmpty()) file = tr("unnamed");
    addRow(tr("File:"),          file);
    addRow(tr("Author:"),        model.author());
    addRow(tr("Organization:"),  model.organization());
    addRow(tr("Circumference:"), QString::number(model.width()));
    addRow(tr("Used rows:"),     QString::number(model.usedHeight()));
    addRow(tr("Repeat (beads):"), QString::number(model.repeat()));

    /*  Per-color inventory ("bead overview") — the block previously
        living in ReportPanel. A QTreeWidget is overkill display-
        wise but gives us free sortable columns and a swatch icon
        in the first column for free.                              */
    auto* tree = new QTreeWidget(this);
    tree->setColumnCount(4);
    tree->setHeaderLabels({tr("Color"), tr("Index"), tr("Symbol"), tr("Count")});
    tree->setRootIsDecorated(false);
    tree->setUniformRowHeights(true);

    BeadCounts counts(model);
    int totalBeads = 0;
    for (int c = 0; c < model.colorCount(); ++c) {
        const int n = counts.count(static_cast<std::int8_t>(c));
        if (n <= 0) continue;
        if (c > 0) totalBeads += n;        // exclude background
        auto* item = new QTreeWidgetItem(tree);
        item->setIcon(0, QIcon(colorSwatch(model.color(c))));
        item->setText(1, QString::number(c));
        item->setText(2, BeadSymbols::glyph(c));
        item->setData(3, Qt::DisplayRole, n);
    }
    tree->resizeColumnToContents(0);
    tree->resizeColumnToContents(1);
    tree->resizeColumnToContents(2);
    tree->resizeColumnToContents(3);

    auto* totals = new QLabel(tr("Total beads (excluding background): %1")
                              .arg(totalBeads), this);

    auto* btns = new QDialogButtonBox(QDialogButtonBox::Close, this);
    connect(btns, &QDialogButtonBox::rejected, this, &QDialog::accept);
    connect(btns, &QDialogButtonBox::accepted, this, &QDialog::accept);

    auto* layout = new QVBoxLayout(this);
    layout->addLayout(form);
    layout->addWidget(tree, 1);
    layout->addWidget(totals);
    layout->addWidget(btns);
    resize(440, 480);
}

} // namespace jbead
