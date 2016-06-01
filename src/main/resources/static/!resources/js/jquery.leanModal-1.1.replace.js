// leanModal v1.1 by Ray Stone - http://finelysliced.com.au
// Dual licensed under the MIT and GPL

// Modify by tinywind

(function ($) {
    window.leanModal = {
        _stackModal: [],
        _stackZIndex: [],
        _zIndexBase: 10000,
        _zIndexInterval: 10,
        getModalZIndex: function (modal) {
            for (var i = 0; i < this._stackModal.length; i++)
                if (this._stackModal[i] == modal) {
                    console.error("already contains modal");
                    return null;
                }
            this._stackModal.push(modal);

            if (this._stackZIndex.length == 0) {
                var zIndex = this._zIndexBase;
                this._stackZIndex.push(zIndex);
                return zIndex;
            } else {
                zIndex = this._stackZIndex[this._stackZIndex.length - 1] + this._zIndexInterval;
                this._stackZIndex.push(zIndex);
                return zIndex;
            }
        },
        checkHideLeanOverlayContainer: function (modal) {
            for (var i = 0; i < this._stackModal.length; i++)
                if (this._stackModal[i] == modal) {
                    this._stackModal.splice(i, 1);
                    this._stackZIndex.splice(i, 1);
                }

            return this._stackModal.length == 0;
        }
    };

    $.fn.extend({
        leanModal: function (options) {
            var defaults = {closeButton: null};
            options = $.extend(defaults, options);
            var overlayContainer = $('#lean-overlay-container');
            if (overlayContainer.length == 0)
                overlayContainer = $('<div/>', {
                    id: 'lean-overlay-container'
                }).bind('mousewheel keydown keyup', function (e) {
                    //e.preventDefault();
                    e.stopPropagation();
                }).css({
                    position: 'fixed',
                    top: 0,
                    bottom: 0,
                    left: 0,
                    right: 0,
                    display: 'none'
                }).appendTo($('body'));

            return this.each(function () {
                var overlay = $('<div/>', {
                    'class': 'lean-overlay'
                }).css({
                    display: "none",
                    opacity: 0.5,
                    position: "absolute",
                    top: 0,
                    bottom: 0,
                    left: 0,
                    right: 0,
                    'background-color': 'black'
                });
                overlayContainer.append(overlay);

                function showModal(modal, overlay) {
                    overlayContainer.show();
                    var zIndex = window.leanModal.getModalZIndex(modal[0]);
                    var top = (window.innerHeight - modal.outerHeight()) / 2;
                    modal.css({
                        'z-index': zIndex,
                        top: top < 0 ? "0px" : top + "px"
                    }).show()
                        .focus();
                    overlay.css({'z-index': zIndex - 1}).show();
                }

                function closeModal(modal, overlay) {
                    modal.hide();
                    overlay.hide();
                    if (overlayContainer.find('.lean-overlay').filter(function () {
                            if (this == overlay[0]) return false;
                            return $(this).is(':hidden');
                        }) == 0)
                        overlayContainer.hide();
                    if (window.leanModal.checkHideLeanOverlayContainer(modal[0]))
                        overlayContainer.hide();
                }

                var href = $(this).attr("href");
                var modal = $(href != null ? href : $(this).attr('data-href'));
                modal.css({
                    display: "none",
                    position: "absolute",
                    left: 50 + "%",
                    'margin-left': -(modal.outerWidth() / 2) + "px"
                }).detach().appendTo(overlayContainer);

                $(this).click(function () {
                    showModal(modal, overlay);
                });

                $(options.closeButton).click(function () {
                    closeModal(modal, overlay);
                });
            });
        }
    })
})(jQuery);
