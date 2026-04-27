#pragma once

#include <QDialog>

class QComboBox;
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
