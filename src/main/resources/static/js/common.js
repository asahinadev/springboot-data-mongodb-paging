function dispLoading(msg) {

	// 引数なし（メッセージなし）を許容
	if (msg == undefined) {
		msg = "";
	}

	// ローディング画像が表示されていない場合のみ出力

	if ($("#loading").length == 0) {
		$("body").append(
				$("<div>").attr("id", "loading").append(
						$("<div>").addClass("loadingMsg").text(msg)).show());

		console.log("show");
	} else {
		console.log("aborted");
	}
}

function closeLoading() {
	setTimeout(function() {
		$("#loading").remove();
		console.log("hide");
	}, 10);
}

setTimeout(function() {
	dispLoading("初期化中");
}, 10);

$(function() {

	$(".table").DataTable({
		scrollY : "450px",
		scrollCollapse : true,
		url : "//cdn.datatables.net/plug-ins/3cfcc339e89/i18n/Japanese.json",
		info : false,
		paging : false,
		searching : false,
		ordering : false
	});

	$(".dataTables_scrollBody").each(function(i, e) {

		var max = $(this).css("max-height");
		$(this).css("min-height", max);

	});

	var form;

	$(".delete-form form").on("submit", function() {

		form = $(this);
		$('#delete_confime').modal('show');
		return false;
	});

	$("#delete_confime .btn-primary").on("click", function() {

		form.off("submit");
		form.submit();

	});

	$(document).on("submit", "form", function() {
		dispLoading("送信中");
	});

	$(".btn-group.radio :checked").each(function(i, e) {
		$(this).parent(".btn").addClass("active");
	});

	if (!Modernizr.inputtypes["date"]) {
		$('input[type=date]').datepicker({
			format : 'yyyy-mm-dd'
		});
	}

	if (!Modernizr.inputtypes["datetime-local"]) {
		$('input[type=datetime-local]').datepicker({
			format : 'yyyy-mm-dd'
		});
	}

	closeLoading();
});
