#include "dialogs.h"

#include <QComboBox>
#include <QDialogButtonBox>
#include <QFormLayout>
#include <QLabel>
#include <QSettings>
#include <QSpinBox>
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

} // namespace jbead
