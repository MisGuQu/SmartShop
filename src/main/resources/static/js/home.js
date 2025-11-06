document.addEventListener("DOMContentLoaded", () => {
    const viewport = document.querySelector("[data-carousel]");
    if (!viewport) {
        return;
    }

    const slides = Array.from(viewport.querySelectorAll(".carousel__slide"));
    const prevButton = document.querySelector("[data-carousel-prev]");
    const nextButton = document.querySelector("[data-carousel-next]");
    const dotsContainer = document.querySelector("[data-carousel-dots]");

    if (!slides.length || !dotsContainer) {
        return;
    }

    let currentIndex = 0;
    const autoplayDelay = 6000;
    let autoplayTimer = null;

    function setActiveSlide(index) {
        slides.forEach((slide, i) => {
            slide.classList.toggle("is-active", i === index);
        });
        const dots = Array.from(dotsContainer.children);
        dots.forEach((dot, i) => {
            dot.classList.toggle("is-active", i === index);
        });
        currentIndex = index;
    }

    function goToNext() {
        const nextIndex = (currentIndex + 1) % slides.length;
        setActiveSlide(nextIndex);
    }

    function goToPrev() {
        const prevIndex = (currentIndex - 1 + slides.length) % slides.length;
        setActiveSlide(prevIndex);
    }

    function stopAutoplay() {
        if (autoplayTimer) {
            window.clearInterval(autoplayTimer);
            autoplayTimer = null;
        }
    }

    function startAutoplay() {
        stopAutoplay();
        autoplayTimer = window.setInterval(goToNext, autoplayDelay);
    }

    function createDots() {
        slides.forEach((_slide, index) => {
            const dot = document.createElement("button");
            dot.className = "carousel__dot";
            dot.type = "button";
            dot.setAttribute("aria-label", `Chuyển tới banner ${index + 1}`);
            dot.addEventListener("click", () => {
                setActiveSlide(index);
                startAutoplay();
            });
            dotsContainer.appendChild(dot);
        });
    }

    createDots();
    setActiveSlide(0);
    startAutoplay();

    nextButton?.addEventListener("click", () => {
        goToNext();
        startAutoplay();
    });

    prevButton?.addEventListener("click", () => {
        goToPrev();
        startAutoplay();
    });

    [viewport, prevButton, nextButton].forEach((element) => {
        element?.addEventListener("mouseenter", stopAutoplay);
        element?.addEventListener("mouseleave", startAutoplay);
        element?.addEventListener("focusin", stopAutoplay);
        element?.addEventListener("focusout", startAutoplay);
    });

    document.addEventListener("visibilitychange", () => {
        if (document.hidden) {
            stopAutoplay();
        } else {
            startAutoplay();
        }
    });
});

