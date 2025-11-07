function formatCurrency(value) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0);
}

const subtotalEl = document.querySelector('[data-checkout-subtotal]');
const shippingEl = document.querySelector('[data-checkout-shipping]');
const totalEl = document.querySelector('[data-checkout-total]');
const shippingRadios = document.querySelectorAll('[data-shipping-option]');

if (subtotalEl && shippingEl && totalEl && shippingRadios.length) {
    const baseSubtotal = Number(subtotalEl.dataset.subtotalValue || 0);

    function recalc() {
        const selected = document.querySelector('[data-shipping-option]:checked');
        const fee = selected ? Number(selected.dataset.fee || 0) : 0;
        shippingEl.textContent = formatCurrency(fee);
        totalEl.textContent = formatCurrency(baseSubtotal + fee);
    }

    shippingRadios.forEach((radio) => radio.addEventListener('change', recalc));
    recalc();
}

