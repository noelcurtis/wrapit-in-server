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
        dateFormat: "mm/dd/yy",
        yearRange: '-1:+1'
    });
    $.extend($.datepicker, {_checkOffset:function(inst,offset,isFixed){return offset}});

    // Active textarea behavior
    $('.form-list textarea').focus(function() {
        console.log($(this).parents(".control-group"))
        $(this).parents(".control-group").each(function() {
            $(this).addClass("active")
        });
    }).blur(function(){
        $(this).parents(".control-group").each(function() {
            $(this).removeClass("active")
        });
    })

    // Focus on the first input
    $('.form-list input').first().focus();

    // If Field is in error state and you start typing clear errors
    $('.form-list input').keypress(function (){
        if ($(this).val() !== ""){
            $(this).parents(".control-group.error").removeClass("error");
        }
    })

});