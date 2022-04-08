$("li.nav-item").hover(
    function () {
        $(this).addClass("hover");
        $(this).children(".subnav").stop(true, true).delay(50).slideDown(50, function () {
            if ($.fn.bgiframe && ($("select").length > 0)) {
                $(this).bgiframe({opacity: false});
            }
        });
    }, function () {
        $(this).removeClass("hover");
        $(this).children(".subnav").stop(true, true).delay(50).slideUp(50);
    });