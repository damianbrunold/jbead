#include "dialogs.h"

#include "beadpainter.h"
#include "colorpickerdialog.h"
#include "domain/beadcounts.h"
#include "domain/beadsymbols.h"
#include "domain/defaultcolors.h"
#include "domain/model.h"
#include "swatchbutton.h"

#include <QComboBox>
#include <QDialogButtonBox>
#include <QFileInfo>
#include <QFormLayout>
#include <QGridLayout>
#include <QHBoxLayout>
#include <QHeaderView>
#include <QLabel>
#include <QPainter>
#include <QPixmap>
#include <QPushButton>
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

// -------------------------------------------------------------------

namespace {

/*  Bigger swatch icon than the toolbar version — gives the editor
    room to show colours legibly even on hidpi screens.            */
QPixmap paletteSwatch(const QColor& color, bool selected, int size = 36)
{
    QPixmap pm(size, size);
    pm.fill(Qt::transparent);
    QPainter p(&pm);
    p.fillRect(2, 2, size - 4, size - 4, color);
    p.setPen(selected ? QPen(Qt::red, 3) : QPen(Qt::black, 1));
    p.drawRect(2, 2, size - 4, size - 4);
    return pm;
}

} // namespace

PaletteEditorDialog::PaletteEditorDialog(Model* model, QWidget* parent)
    : QDialog(parent), m_model(model), m_grid(new QGridLayout)
{
    setWindowTitle(tr("Palette"));
    m_selected = m_model->selectedColor();

    auto* hint = new QLabel(
        tr("Click a swatch to select it; double-click to edit its colour."), this);
    hint->setWordWrap(true);

    auto* gridHost = new QWidget(this);
    gridHost->setLayout(m_grid);
    rebuild();

    auto* btns = new QDialogButtonBox(this);
    auto* editBtn = btns->addButton(tr("&Edit colour..."), QDialogButtonBox::ActionRole);
    auto* resetBtn = btns->addButton(tr("&Restore defaults"), QDialogButtonBox::ResetRole);
    btns->addButton(QDialogButtonBox::Close);
    connect(editBtn,  &QPushButton::clicked, this, [this]() { editEntry(m_selected); });
    connect(resetBtn, &QPushButton::clicked, this, &PaletteEditorDialog::restoreDefaults);
    connect(btns,     &QDialogButtonBox::rejected, this, &QDialog::accept);
    connect(btns,     &QDialogButtonBox::accepted, this, &QDialog::accept);

    auto* root = new QVBoxLayout(this);
    root->addWidget(hint);
    root->addWidget(gridHost, 1);
    root->addWidget(btns);
    resize(440, 320);

    /*  Live update: if anything else changes the palette while
        the dialog is open (e.g. the toolbar swatch double-click,
        or a model load), refresh.                                 */
    connect(m_model, &Model::colorChanged,  this, [this](int) { rebuild(); });
    connect(m_model, &Model::colorsChanged, this, &PaletteEditorDialog::rebuild);
    connect(m_model, &Model::modelChanged,  this, &PaletteEditorDialog::rebuild);
    /*  Stay in sync with selection changes from outside — e.g.
        user clicks a swatch in the toolbar while this dialog is
        open, or the pipette tool fires.                          */
    connect(m_model, &Model::selectedColorChanged, this, [this](int idx) {
        m_selected = idx;
        for (int i = 0; i < m_buttons.size(); ++i) {
            m_buttons[i]->setIcon(paletteSwatch(m_model->color(i), i == m_selected));
        }
    });
}

void PaletteEditorDialog::rebuild()
{
    /*  Tear down and re-create the grid each time so adding /
        removing palette entries (via Model::loadFrom for a file
        with fewer colours) updates the layout cleanly.            */
    while (auto* item = m_grid->takeAt(0)) {
        if (auto* w = item->widget()) w->deleteLater();
        delete item;
    }
    m_buttons.clear();

    constexpr int COLS = 8;
    for (int i = 0; i < m_model->colorCount(); ++i) {
        auto* btn = new SwatchButton;
        btn->setAutoRaise(true);
        btn->setIconSize(QSize(36, 36));
        btn->setIcon(paletteSwatch(m_model->color(i), i == m_selected));
        btn->setToolTip(tr("Color %1 — double-click to edit").arg(i));
        connect(btn, &QToolButton::clicked, this,
                [this, i]() { selectEntry(i); });
        connect(btn, &SwatchButton::doubleClicked, this,
                [this, i]() { selectEntry(i); editEntry(i); });
        m_grid->addWidget(btn, i / COLS, i % COLS);
        m_buttons.append(btn);
    }
}

void PaletteEditorDialog::selectEntry(int index)
{
    if (index < 0 || index >= m_buttons.size()) return;
    m_selected = index;
    m_model->setSelectedColor(static_cast<std::int8_t>(index));
    /*  Just refresh the icons rather than rebuilding the whole
        grid — preserves focus and keeps the click responsive.    */
    for (int i = 0; i < m_buttons.size(); ++i) {
        m_buttons[i]->setIcon(paletteSwatch(m_model->color(i), i == m_selected));
    }
}

void PaletteEditorDialog::editEntry(int index)
{
    if (index < 0 || index >= m_model->colorCount()) return;
    const QColor picked = ColorPickerDialog::getColor(
        m_model->color(index), this, tr("Pick color %1").arg(index));
    if (!picked.isValid()) return;
    m_model->setColor(index, picked);
    /*  Model emits colorChanged; the colorChanged slot above
        triggers rebuild(). No explicit refresh needed here.       */
}

void PaletteEditorDialog::restoreDefaults()
{
    /*  Apply each entry through Model::setColor so every change
        is undoable (one snapshot per slot — coarse but good
        enough for a "reset" button the user rarely hits).        */
    const QList<QColor> defaults = DefaultColors::palette();
    const int n = std::min<int>(defaults.size(), m_model->colorCount());
    for (int i = 0; i < n; ++i) {
        if (m_model->color(i) != defaults.at(i)) {
            m_model->setColor(i, defaults.at(i));
        }
    }
}

} // namespace jbead
