#include "colorpickerdialog.h"

#include <QDialogButtonBox>
#include <QGridLayout>
#include <QHBoxLayout>
#include <QLabel>
#include <QLineEdit>
#include <QMouseEvent>
#include <QPainter>
#include <QPalette>
#include <QRegularExpression>
#include <QRegularExpressionValidator>
#include <QSlider>
#include <QSpinBox>
#include <QVBoxLayout>

#include <algorithm>
#include <cmath>

namespace jbead {

// =================================================================
// HueWheel — full-circle pie chart, click anywhere to set hue.
// =================================================================

class HueWheel : public QWidget
{
public:
    explicit HueWheel(ColorPickerDialog* owner) : QWidget(owner), m_owner(owner)
    {
        setMinimumSize(160, 160);
    }

protected:
    void paintEvent(QPaintEvent*) override
    {
        QPainter p(this);
        p.setRenderHint(QPainter::Antialiasing);
        const int w = width(), h = height();
        const int r = std::min(w, h) - 4;
        const QRect box((w - r) / 2, (h - r) / 2, r, r);

        /*  360 1-degree pie slices — simpler than per-pixel and
            looks plenty smooth at typical sizes.                  */
        for (int i = 0; i < 360; ++i) {
            p.setPen(Qt::NoPen);
            p.setBrush(QColor::fromHsv(i, 255, 255));
            /*  Qt angles are 1/16 degree, 0 at 3 o'clock, CCW.  */
            p.drawPie(box, i * 16, 16);
        }

        /*  Hue indicator. */
        const double phi = -m_owner->hue() * M_PI / 180.0;
        const int cx = box.center().x();
        const int cy = box.center().y();
        const int rout = r / 2;
        const QPoint p1(cx + int(std::cos(phi) * rout),
                        cy + int(std::sin(phi) * rout));
        const QPoint p2(cx + int(std::cos(phi) * (rout - 14)),
                        cy + int(std::sin(phi) * (rout - 14)));
        p.setPen(QPen(m_owner->val() > 120 ? Qt::black : Qt::white, 2));
        p.drawLine(p1, p2);
    }

    void mousePressEvent(QMouseEvent* e) override   { handleMouse(e); }
    void mouseMoveEvent(QMouseEvent* e) override
    {
        if (e->buttons() & Qt::LeftButton) handleMouse(e);
    }

private:
    /*  Renamed from update() — that name shadows QWidget::update()
        and broke ColorPickerDialog::updateAll's m_wheel->update()
        repaint trigger.                                            */
    void handleMouse(QMouseEvent* e)
    {
        const int cx = width()  / 2;
        const int cy = height() / 2;
        const int dx = int(e->position().x()) - cx;
        const int dy = int(e->position().y()) - cy;
        if (dx == 0 && dy == 0) return;
        double phi = std::atan2(double(-dy), double(dx));
        if (phi < 0) phi += 2.0 * M_PI;
        const int h = int(360.0 * phi / (2.0 * M_PI)) % 360;
        m_owner->setHSV(h, m_owner->sat(), m_owner->val());
    }

    ColorPickerDialog* m_owner;
};

// =================================================================
// SatValPatch — saturation (x-axis) by value (y-axis) square.
// =================================================================

class SatValPatch : public QWidget
{
public:
    explicit SatValPatch(ColorPickerDialog* owner) : QWidget(owner), m_owner(owner)
    {
        setMinimumSize(140, 140);
    }

protected:
    void paintEvent(QPaintEvent*) override
    {
        QPainter p(this);
        const int w = width(), h = height();
        const int hue = m_owner->hue();
        /*  2x2 cells — much faster than per-pixel and visually
            indistinguishable at this resolution.                  */
        for (int y = 0; y < h; y += 2) {
            const int v = 255 - 255 * y / h;
            for (int x = 0; x < w; x += 2) {
                const int s = 255 * x / w;
                p.fillRect(x, y, 2, 2, QColor::fromHsv(hue, s, v));
            }
        }
        const int mx = w * m_owner->sat() / 255;
        const int my = h * (255 - m_owner->val()) / 255;
        p.setPen(QPen(m_owner->val() > 120 ? Qt::black : Qt::white, 2));
        p.setBrush(Qt::NoBrush);
        p.drawEllipse(QPoint(mx, my), 5, 5);
    }

    void mousePressEvent(QMouseEvent* e) override   { handleMouse(e); }
    void mouseMoveEvent(QMouseEvent* e) override
    {
        if (e->buttons() & Qt::LeftButton) handleMouse(e);
    }

private:
    void handleMouse(QMouseEvent* e)
    {
        const int s = std::clamp(int(255.0 * e->position().x() / width()),  0, 255);
        const int v = std::clamp(int(255.0 * (height() - e->position().y()) / height()), 0, 255);
        m_owner->setHSV(m_owner->hue(), s, v);
    }

    ColorPickerDialog* m_owner;
};

// =================================================================
// ColorPickerDialog
// =================================================================

ColorPickerDialog::ColorPickerDialog(const QColor& initial, QWidget* parent,
                                     const QString& title)
    : QDialog(parent), m_color(initial.isValid() ? initial : QColor(Qt::black))
{
    setWindowTitle(title.isEmpty() ? tr("Pick colour") : title);
    setModal(true);
    buildUi();
    updateAll();
}

QColor ColorPickerDialog::getColor(const QColor& initial, QWidget* parent,
                                   const QString& title)
{
    ColorPickerDialog dlg(initial, parent, title);
    if (dlg.exec() == QDialog::Accepted) return dlg.selectedColor();
    return QColor();
}

int ColorPickerDialog::hue() const { const int h = m_color.hue(); return h < 0 ? 0 : h; }
int ColorPickerDialog::sat() const { return m_color.saturation(); }
int ColorPickerDialog::val() const { return m_color.value(); }

void ColorPickerDialog::setHSV(int h, int s, int v)
{
    setColor(QColor::fromHsv(std::clamp(h, 0, 359),
                             std::clamp(s, 0, 255),
                             std::clamp(v, 0, 255)));
}

void ColorPickerDialog::buildUi()
{
    m_wheel   = new HueWheel(this);
    m_patch   = new SatValPatch(this);
    m_preview = new QWidget(this);
    m_preview->setAutoFillBackground(true);
    m_preview->setMinimumSize(120, 80);

    m_hex = new QLineEdit(this);
    /*  #RRGGBB or RRGGBB — case-insensitive, 6 hex digits with
        optional leading '#'. The validator stops the user from
        typing nonsense; the explicit color check on commit
        handles the partial-input case where the validator allows
        empty/short values via Intermediate.                       */
    auto* hexValidator = new QRegularExpressionValidator(
        QRegularExpression(QStringLiteral("#?[0-9A-Fa-f]{0,6}")), this);
    m_hex->setValidator(hexValidator);
    m_hex->setMaxLength(7);
    m_hex->setPlaceholderText(QStringLiteral("#RRGGBB"));

    auto makeSlider = [&](QSlider*& slider, QSpinBox*& spin, int max) {
        slider = new QSlider(Qt::Horizontal, this);
        slider->setRange(0, max);
        spin = new QSpinBox(this);
        spin->setRange(0, max);
        spin->setMaximumWidth(64);
        connect(slider, &QSlider::valueChanged,
                spin, [spin, slider](int v) { QSignalBlocker b(spin); spin->setValue(v); });
        connect(spin, qOverload<int>(&QSpinBox::valueChanged),
                slider, [slider, spin](int v) { QSignalBlocker b(slider); slider->setValue(v); });
    };
    makeSlider(m_slH, m_spnH, 359);
    makeSlider(m_slS, m_spnS, 255);
    makeSlider(m_slV, m_spnV, 255);
    makeSlider(m_slR, m_spnR, 255);
    makeSlider(m_slG, m_spnG, 255);
    makeSlider(m_slB, m_spnB, 255);

    /*  Single funnel: any user-driven change reads back through
        the controls and rebuilds the colour. The setColor() body
        re-pushes the canonical state into every widget under
        signal blockers, so this can't loop.                      */
    auto onHsvSlider = [this]() {
        setColor(QColor::fromHsv(m_slH->value(), m_slS->value(), m_slV->value()));
    };
    auto onRgbSlider = [this]() {
        setColor(QColor(m_slR->value(), m_slG->value(), m_slB->value()));
    };
    connect(m_slH, &QSlider::valueChanged, this, onHsvSlider);
    connect(m_slS, &QSlider::valueChanged, this, onHsvSlider);
    connect(m_slV, &QSlider::valueChanged, this, onHsvSlider);
    connect(m_slR, &QSlider::valueChanged, this, onRgbSlider);
    connect(m_slG, &QSlider::valueChanged, this, onRgbSlider);
    connect(m_slB, &QSlider::valueChanged, this, onRgbSlider);
    connect(m_hex, &QLineEdit::editingFinished, this, [this]() {
        QString txt = m_hex->text().trimmed();
        if (!txt.startsWith(QLatin1Char('#'))) txt.prepend(QLatin1Char('#'));
        if (txt.size() != 7) { updateAll(); return; } // revert on partial
        QColor c(txt);
        if (c.isValid()) setColor(c);
        else updateAll();
    });

    auto* btns = new QDialogButtonBox(QDialogButtonBox::Ok | QDialogButtonBox::Cancel, this);
    connect(btns, &QDialogButtonBox::accepted, this, &QDialog::accept);
    connect(btns, &QDialogButtonBox::rejected, this, &QDialog::reject);

    auto labelled = [&](const QString& text, QSlider* sl, QSpinBox* sp) {
        auto* row = new QHBoxLayout;
        auto* lbl = new QLabel(text, this);
        lbl->setMinimumWidth(70);
        row->addWidget(lbl);
        row->addWidget(sl, 1);
        row->addWidget(sp);
        return row;
    };

    auto* hsvBlock = new QVBoxLayout;
    hsvBlock->addWidget(new QLabel(tr("HSV"), this));
    hsvBlock->addLayout(labelled(tr("&Hue:"),         m_slH, m_spnH));
    hsvBlock->addLayout(labelled(tr("&Saturation:"),  m_slS, m_spnS));
    hsvBlock->addLayout(labelled(tr("&Value:"),       m_slV, m_spnV));

    auto* rgbBlock = new QVBoxLayout;
    rgbBlock->addWidget(new QLabel(tr("RGB"), this));
    rgbBlock->addLayout(labelled(tr("&Red:"),    m_slR, m_spnR));
    rgbBlock->addLayout(labelled(tr("&Green:"),  m_slG, m_spnG));
    rgbBlock->addLayout(labelled(tr("&Blue:"),   m_slB, m_spnB));

    auto* previewBlock = new QVBoxLayout;
    previewBlock->addWidget(new QLabel(tr("Preview"), this), 0, Qt::AlignHCenter);
    previewBlock->addWidget(m_preview, 1);
    auto* hexRow = new QHBoxLayout;
    hexRow->addWidget(new QLabel(tr("He&x:"), this));
    hexRow->addWidget(m_hex);
    previewBlock->addLayout(hexRow);

    auto* topGrid = new QGridLayout;
    topGrid->addWidget(new QLabel(tr("Hue"),                this), 0, 0, Qt::AlignHCenter);
    topGrid->addWidget(new QLabel(tr("Saturation / Value"), this), 0, 1, Qt::AlignHCenter);
    topGrid->addLayout(previewBlock,                              0, 2, 2, 1);
    topGrid->addWidget(m_wheel, 1, 0);
    topGrid->addWidget(m_patch, 1, 1);

    auto* sliders = new QHBoxLayout;
    sliders->addLayout(hsvBlock);
    sliders->addSpacing(16);
    sliders->addLayout(rgbBlock);

    auto* root = new QVBoxLayout(this);
    root->addLayout(topGrid, 1);
    root->addLayout(sliders);
    root->addWidget(btns);
    resize(620, 480);
}

void ColorPickerDialog::setColor(const QColor& c)
{
    if (!c.isValid()) return;
    m_color = c;
    updateAll();
}

void ColorPickerDialog::updateAll()
{
    /*  Push canonical state into every widget under signal
        blockers — handlers never re-fire from these writes. The
        wheel / patch are repainted because hue / sat / val are
        derived from m_color via hue()/sat()/val().               */
    const int h = hue(), s = sat(), v = val();
    const int r = m_color.red(), g = m_color.green(), b = m_color.blue();
    auto block = [](QObject* o) { return QSignalBlocker(o); };
    {
        auto _1 = block(m_slH); auto _2 = block(m_spnH); m_slH->setValue(h); m_spnH->setValue(h);
        auto _3 = block(m_slS); auto _4 = block(m_spnS); m_slS->setValue(s); m_spnS->setValue(s);
        auto _5 = block(m_slV); auto _6 = block(m_spnV); m_slV->setValue(v); m_spnV->setValue(v);
        auto _7 = block(m_slR); auto _8 = block(m_spnR); m_slR->setValue(r); m_spnR->setValue(r);
        auto _9 = block(m_slG); auto _10 = block(m_spnG); m_slG->setValue(g); m_spnG->setValue(g);
        auto _11 = block(m_slB); auto _12 = block(m_spnB); m_slB->setValue(b); m_spnB->setValue(b);
        auto _13 = block(m_hex);
        m_hex->setText(QStringLiteral("#%1%2%3")
            .arg(r, 2, 16, QLatin1Char('0'))
            .arg(g, 2, 16, QLatin1Char('0'))
            .arg(b, 2, 16, QLatin1Char('0')).toUpper());
    }
    QPalette pal = m_preview->palette();
    pal.setColor(QPalette::Window, m_color);
    m_preview->setPalette(pal);
    m_preview->update();
    m_wheel->update();
    m_patch->update();
}

} // namespace jbead
