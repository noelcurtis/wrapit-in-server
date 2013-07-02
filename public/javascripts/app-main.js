function toggleCheck(e)
{
    var label = $(e).find("label.checkbox").first();
    if ($(label).hasClass("checked"))
    {
        $(label).removeClass("checked");
    }
    else
    {
        $(label).addClass("checked");
    }
}