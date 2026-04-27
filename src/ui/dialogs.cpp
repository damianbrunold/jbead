#include "dialogs.h"

#include <QDialogButtonBox>
#include <QFormLayout>
#include <QLabel>
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

} // namespace jbead
