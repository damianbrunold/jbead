#pragma once

#include <QDialog>

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

} // namespace jbead
