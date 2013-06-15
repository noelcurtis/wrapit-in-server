$(function(){

    // Inputs behavior
    $('.form-list input[type="text"], .form-list textarea').focus(function(){
            $(this).parents(".control-group").addClass("active");
    }
    ).blur(function(){
            $(this).parents(".control-group").removeClass("active");
        }
    );


    // Datepicker for due date
    $('#datepicker-duedate').datepicker({
        showOtherMonths: true,
        selectOtherMonths: true,
        dateFormat: "dd/mm/yy",
        yearRange: '-1:+1'
    });
    $.extend($.datepicker, {_checkOffset:function(inst,offset,isFixed){return offset}});

    $('.form-list textarea').focus(function() {
        console.log($(this).parents(".control-group"))
        $(this).parents(".control-group").each(function() {
            console.log(this)
            $(this).addClass("active")
        });
    }).blur(function(){
        $(this).parents(".control-group").each(function() {
            $(this).removeClass("active")
        });
    })

});