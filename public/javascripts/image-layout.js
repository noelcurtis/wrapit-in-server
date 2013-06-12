$(
    function (){
        var $container = $('#fluid-image-container');

        $container.imagesLoaded( function(){
            $container.masonry({
                itemSelector : '.photo-box'
            });
        });
    }
)

