#pragma once

#include <QDialog>

class QComboBox;
class QGridLayout;
class QSpinBox;

namespace jbead {

/*  Edit -> Arrange dialog. Two integer inputs (copies, offset)
    forwarded to Model::arrangeSelection.                          */
class ArrangeDialog : public QDialog
{
    Q_OBJECT
public:
    explicit ArrangeDialog(int defaultCopies, int defaultOffset, QWidget* parent = nullptr);
    int copies() const;
    int offset() const;

private:
    QSpinBox* m_copies;
    QSpinBox* m_offset;
};

/*  Pattern -> Width / Height dialogs. Single spinbox each.       */
class IntPromptDialog : public QDialog
{
    Q_OBJECT
public:
    IntPromptDialog(const QString& title, const QString& label,
                    int currentValue, int minValue, int maxValue,
                    QWidget* parent = nullptr);
    int value() const;

private:
    QSpinBox* m_value;
};

class Model;

/*  Pattern -> Palette editor. Grid of all palette entries (32
    swatches in 8 columns x 4 rows). Single-click selects a swatch
    in the model so the rest of the editor sees the change; double-
    click (and the "Edit colour..." button) opens a QColorDialog to
    pick a new RGB. "Restore defaults" rebinds every entry to the
    DefaultColors palette. All edits route through Model::setColor
    so they emit colorChanged and live-update every canvas behind
    the modeless dialog (and they snapshot for undo).             */
class PaletteEditorDialog : public QDialog
{
    Q_OBJECT
public:
    explicit PaletteEditorDialog(Model* model, QWidget* parent = nullptr);

private slots:
    void rebuild();
    void editEntry(int index);
    void selectEntry(int index);
    void restoreDefaults();

private:
    Model*                  m_model;
    QGridLayout*            m_grid;
    QList<class SwatchButton*> m_buttons;
    int                     m_selected = 0;
};

/*  Info -> Technical Infos dialog. Read-only summary of the
    current pattern: file metadata (path, author, organization,
    notes), high-level counts (circumference, used rows, total
    beads, repeat unit), plus a per-palette-entry bead inventory
    that mirrors what the legacy desktop's "bead overview" panel
    showed.                                                       */
class TechInfosDialog : public QDialog
{
    Q_OBJECT
public:
    explicit TechInfosDialog(const Model& model, QWidget* parent = nullptr);
};

/*  Edit / Pattern -> Preferences dialog. Two persistent settings:

      - UI language: "system" (follow QLocale), "en", "de", or "fr".
        Stored as Environment/Language. Requires an app restart since
        QTranslator is wired up in main().
      - Color scheme: "system", "light", or "dark". Stored as
        Environment/ColorScheme and applied live via
        QGuiApplication::styleHints()->setColorScheme().
*/
class PreferencesDialog : public QDialog
{
    Q_OBJECT
public:
    explicit PreferencesDialog(QWidget* parent = nullptr);

    QString language()    const;   // "system" / "en" / "de" / "fr"
    QString colorScheme() const;   // "system" / "light" / "dark"

private:
    QComboBox* m_language;
    QComboBox* m_colorScheme;
};

} // namespace jbead
