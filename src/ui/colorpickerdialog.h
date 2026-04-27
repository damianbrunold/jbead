#pragma once

#include <QColor>
#include <QDialog>

class QLineEdit;
class QSlider;
class QSpinBox;
class QWidget;

namespace jbead {

/*  Custom HSV / RGB colour picker, ported from dbweave's
    ChooseHSVDialog (src/ui/choosecolordialog.cpp) with an extra
    RGB section so the user can dial in a colour either way.

    Layout:

        [hue wheel] [sat/val patch]  [preview]
                                     [hex #RRGGBB]
        H slider [value]   R slider [value]
        S slider [value]   G slider [value]
        V slider [value]   B slider [value]
                  [OK]  [Cancel]

    All controls share a single live QColor — editing any of them
    propagates to the others through a single setColor() funnel
    that uses QSignalBlocker to avoid feedback loops.            */
class ColorPickerDialog : public QDialog
{
    Q_OBJECT
public:
    explicit ColorPickerDialog(const QColor& initial, QWidget* parent = nullptr,
                               const QString& title = QString());

    QColor selectedColor() const { return m_color; }

    /*  Convenience (matches QColorDialog::getColor): returns the
        picked colour or an invalid QColor if cancelled.          */
    static QColor getColor(const QColor& initial, QWidget* parent = nullptr,
                           const QString& title = QString());

    /*  Hue wheel and sat/val patch read these via the public API
        (matches dbweave's pattern — the widgets are owned by the
        dialog and call back into it).                             */
    int hue() const;
    int sat() const;
    int val() const;
    void setHSV(int h, int s, int v);

private:
    void buildUi();
    void setColor(const QColor& c);
    void updateAll();

    QColor                  m_color;

    class HueWheel*         m_wheel    = nullptr;
    class SatValPatch*      m_patch    = nullptr;
    QWidget*                m_preview  = nullptr;
    QLineEdit*              m_hex      = nullptr;

    QSlider*  m_slH = nullptr; QSpinBox* m_spnH = nullptr;
    QSlider*  m_slS = nullptr; QSpinBox* m_spnS = nullptr;
    QSlider*  m_slV = nullptr; QSpinBox* m_spnV = nullptr;
    QSlider*  m_slR = nullptr; QSpinBox* m_spnR = nullptr;
    QSlider*  m_slG = nullptr; QSpinBox* m_spnG = nullptr;
    QSlider*  m_slB = nullptr; QSpinBox* m_spnB = nullptr;
};

} // namespace jbead
