#include "beadfield.h"

#include "rectiterator.h"

#include <algorithm>
#include <cstring>

namespace jbead {

BeadField::BeadField()
    : m_field(DEFAULT_WIDTH * DEFAULT_HEIGHT, '\0'),
      m_width(DEFAULT_WIDTH),
      m_height(DEFAULT_HEIGHT)
{
}

void BeadField::clear()
{
    std::memset(m_field.data(), 0, m_field.size());
}

void BeadField::setWidth(int width)
{
    QByteArray fresh(width * m_height, '\0');
    const int span = std::min(width, m_width);
    for (int j = 0; j < m_height; ++j) {
        std::memcpy(fresh.data() + j * width,
                    m_field.constData() + j * m_width,
                    span);
    }
    m_field = std::move(fresh);
    m_width = width;
}

void BeadField::setHeight(int height)
{
    if (m_height == height) return;
    QByteArray fresh(m_width * height, '\0');
    std::memcpy(fresh.data(), m_field.constData(),
                m_width * std::min(m_height, height));
    m_field  = std::move(fresh);
    m_height = height;
}

void BeadField::copyFrom(const BeadField& source)
{
    setWidth(source.m_width);
    setHeight(source.m_height);
    m_field = source.m_field;
}

void BeadField::swap(BeadPoint a, BeadPoint b)
{
    const std::int8_t tmp = get(a);
    set(a, get(b));
    set(b, tmp);
}

void BeadField::mirrorHorizontal(const BeadRect& rect)
{
    for (int j = rect.bottom(); j <= rect.top(); ++j) {
        for (int i = rect.left(); i <= (rect.left() + rect.right()) / 2; ++i) {
            swap(BeadPoint(i, j), BeadPoint(rect.right() - (i - rect.left()), j));
        }
    }
}

void BeadField::mirrorVertical(const BeadRect& rect)
{
    for (int i = rect.left(); i <= rect.right(); ++i) {
        for (int j = rect.bottom(); j <= (rect.bottom() + rect.top()) / 2; ++j) {
            swap(BeadPoint(i, j), BeadPoint(i, rect.top() - (j - rect.bottom())));
        }
    }
}

void BeadField::rotate(const BeadRect& rect)
{
    if (!rect.isSquare()) return;
    const QByteArray buffer = copyOf(rect);
    for (int j = 0; j < rect.height(); ++j) {
        for (int i = 0; i < rect.width(); ++i) {
            const int x = j;
            const int y = rect.height() - 1 - i;
            set(BeadPoint(rect.left() + x, rect.bottom() + y),
                static_cast<std::int8_t>(buffer.at(j * rect.width() + i)));
        }
    }
}

void BeadField::deleteRect(const BeadRect& rect)
{
    RectIterator it(rect);
    while (it.hasNext()) {
        set(it.next(), 0);
    }
}

void BeadField::insertRow()
{
    for (int j = m_height - 1; j > 0; --j) {
        for (int i = 0; i < m_width; ++i) {
            const BeadPoint pt(i, j);
            set(pt, get(pt.nextBelow()));
        }
    }
    for (int i = 0; i < m_width; ++i) set(BeadPoint(i, 0), 0);
}

void BeadField::deleteRow()
{
    for (int j = 0; j < m_height - 1; ++j) {
        for (int i = 0; i < m_width; ++i) {
            const BeadPoint pt(i, j);
            set(pt, get(pt.nextAbove()));
        }
    }
    for (int i = 0; i < m_width; ++i) set(BeadPoint(i, m_height - 1), 0);
}

void BeadField::replaceColor(std::int8_t oldColor, std::int8_t newColor)
{
    char* p = m_field.data();
    const int n = m_field.size();
    for (int i = 0; i < n; ++i) {
        if (p[i] == static_cast<char>(oldColor)) p[i] = static_cast<char>(newColor);
    }
}

QByteArray BeadField::copyOf(const BeadRect& rect) const
{
    QByteArray out(rect.size(), '\0');
    for (int j = 0; j < rect.height(); ++j) {
        for (int i = 0; i < rect.width(); ++i) {
            out[j * rect.width() + i] =
                m_field.at(indexOf(BeadPoint(rect.left() + i, rect.bottom() + j)));
        }
    }
    return out;
}

void BeadField::setData(const QByteArray& data, int width, int height)
{
    m_field  = data;
    m_width  = width;
    m_height = height;
    if (m_field.size() != m_width * m_height) {
        m_field.resize(m_width * m_height);
    }
}

} // namespace jbead
