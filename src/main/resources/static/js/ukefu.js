var layer , iframe , layerwin , cursession ;
$(document).ready(function(){
	var hide ;
	$('.dropdown-menu').on("click" , function(){
		var distance = getDistance(this);
		if(hide = true){
			$(this).closest(".ukefu-btn-group").addClass("open");
		}else{
			$(this).closest(".ukefu-btn-group").removeClass("open");
		}
		if(distance.right < 200){
			$(this).next().css("right" , "0px").css("left" , "auto");
		}
	}).hover(function(){
		hide = true ;
	} , function(){
		hide = false ;
		var btn = $(this); 
		setTimeout(function(){
			if(hide){
				$(btn).removeClass("open");
			}
		} , 500);
	});
	$('.ukefu-btn-group').hover(function(){
		$(this).addClass("open");
		$(this).find('.ukefu-dropdown-menu').css("right" , "0px").css("left" , "auto");
		hide = false ;
	} , function(){
		hide = true ;
		setTimeout(function(){
			if(hide){
				$(".ukefu-btn-group.open").removeClass("open");
			}
		} , 500);
	});
	layui.use(['layer'], function(){
		layer = layui.layer;	 	 
	});

	$(document).on('click','[data-toggle="ajax"]', function ( e ) {
		var url = $(this).attr("href");
		if(url && url != "javascript:void(0)"){
			var title = $(this).attr("title") ? $(this).attr("title") : $(this).attr("data-title");
			var artwidth = $(this).attr("data-width") ? $(this).attr("data-width") : 800 ;
			var artheight = $(this).attr("data-height") ? $(this).attr("data-height") : 400 ;
			var target = $(this) ;
			top.iframe = window.frameElement && window.frameElement.id || '';
			$.ajax({
				url:url,
				cache:false,
				success: function(data){
					var multiTitle = $(target).data("multi") ;
					if(multiTitle){
						var img = $(target).data("icon") ? $(target).data("icon") : "/images/workorders.png" ;
						var name = title ;
						var text = $(target).data("text") ? $(target).data("text") : title ;
						top.layerwin = top.layer.open({
							title: ["<div style='position: relative;height: 42px;padding: 5px 15px 5px 0px;line-height: 20px;cursor: pointer;display: inline-block;vertical-align: top;'><img src='"+img+"' style='max-height:50px;'><div style='padding:0px 5px;line-height: 23px;display: inline-block;vertical-align: top;'><span style='vertical-align: top;font-size:18px;'>"+name+"</span><p style='vertical-align: top;font-size: 12px;color: #999;'>"+text+"</p></div></div>" , "height:55px"], 
							type: 1, 
							maxmin: true, 
							anim: 2,
							id: 'mainajaxwin', 
							area:[artwidth+"px" , artheight+"px"] ,
							content: data}
						);
					}else{
						top.layerwin = top.layer.open({title:title, type: 1, id: 'mainajaxwin', area:[artwidth+"px" , artheight+"px"] , maxmin: true, anim: 2,content: data});
					}
				}
			});
		}
		
		return false;
	});
	
	$(document).on('click','[data-toggle="load"]', function ( e ) {
		var url = $(this).attr("href");
		var target = $(this).data("target");
		var callback = $(this).data("callback");
		var index = top.layer.load(0, {shade: false});
		$.ajax({
			url:url,
			cache:false,
			success: function(data){
				$(target).empty().html(data);
				top.layer.close(index);
				if(callback){
					eval(callback);
				}
			}
		});
		
		return false;
	});
	
	$(document).on('click','[data-toggle="tip"]', function ( e ) {
		var title = $(this).attr("title") ? $(this).attr("title") : $(this).attr("data-title");
		var href = 	$(this).attr('href')  ;
		var confirm = $(this).data('confirm')  ;
		var target = $(this).data('target')  ;
		if(href == null){
			href = $(this).data('href') ;
		}
		var callback = $(this).data('callback') ;
		top.layer.confirm(title, {icon: 3, title:'提示'}, function(index){
			top.layer.close(index);
			if(confirm){
				top.layer.prompt({title: confirm, formType:1}, function(text, cindex){
					top.layer.close(cindex);
					if(href){
						if(href.indexOf("?") > 0){
							href = href + "&confirm="+text ;
						}else{
							href = href + "?confirm="+text ;
						}
						if(callback!=null){
							eval(callback+"('"+href+"' , '"+target+"')");
						}else{
							location.href = href ;
						}
					}
					
				});
			}else{
				if(href){
					if(callback!=null){
						eval(callback+"('"+href+"' , '"+target+"')");
					}else{
						location.href = href ;
					}
				}
			}
		});
		return false;
	});
	$(document).on('submit.form.data-api','form', function ( e ) {
		var formValue = $(e.target) ;
		var close = $(this).data("close");
		if(iframe){
			$(e.target).attr('target' , iframe);
		}
		if(layerwin && close == null){
			layer.close(layerwin);
		}
	});
	
	/**
	 *表单验证
	 *
	 *
	 */
	$(document).on('submit.form.data-api','[data-toggle="ajax-form"]', function ( e ) {
		var formValue = $(e.target) ;
		var target = $(this).data("target");
		var inner = $(this).data("inner");
		var close = $(this).data("close");
		var index ;
		if(close == null){
			index = top.layer.load(0, {shade: false});
		}
		$(this).ajaxSubmit({	  
			url:formValue.attr("action"),
			success: function(data){
				if(target){
					$(target).empty().append(data) ;
				}else if(inner){
					var targetIFrame = eval(iframe);
					targetIFrame.Proxy.updateData(inner , data) ;
				}
				if(close == null || close == true){
					if(close == null){
						top.layer.close(index);
					}else{
						layer.close(layer.index);
					}
					top.layer.alert('保存成功', {icon: 1});
				}
			},
			error:function(xhr, type, s){  				
				//notification("",false);	//结束
			}
		}); 
		return false;
	});
	
	$(document).on('click','.ukefu-media-image , .workorders-content img', function ( e ) {
		var html = $(this).attr("src");
		top.layer.open({
			  type: 1,
			  title: "图片",
			  closeBtn: 1,
			  maxmin: true,
			  area: ['750px','500px'],
			  shadeClose: true,
			  content: "<div class='ukefu-preview-image'><img src='"+html+"_original'></div>"
			});
	});
	
	function getDistance(obj) {  
		 var distance = {};  
		 distance.top = ($(obj).offset().top - $(document).scrollTop());  
		 distance.bottom = ($(window).height() - distance.top - $(obj).outerHeight());  
		 distance.left = ($(obj).offset().left - $(document).scrollLeft());  
		 distance.right = ($(window).width() - distance.left - $(obj).outerWidth());  
		 return distance;  
	}
});

function loadURL(url , panel , callback  , append){
	loadURLWithTip(url  , panel , callback , append , false) ;
}

function loadURLWithTip(url , panel , callback , append  , tip){
	var inx ;
	if(tip){
		index = top.layer.load(0, {shade: false});
	}
	$.ajax({
		url:url,
		cache:false,
		success: function(data){
			if(tip){
				top.layer.close(index);
			}
			if(panel){
				if(append){
					$(panel).append(data);
				}else{
					$(panel).empty().html(data);
				}
			}
			if(callback){
				callback(data);			
			}
		},
		error:  function(xhr, type, s){	
			if(xhr.getResponseHeader("emsg")){
				art.alert(xhr.getResponseHeader("emsg"));
			}
		}
	}).done(function(){
		
	});
}
var Proxy = {
	newAgentUserService:function(data){
		if($('#tip_message_'+data.userid).length >0){
			$('#tip_message_'+data.userid).removeClass('bg-gray').addClass("bg-green").text('在线');
		}else{
			if($('.chat-list-item.active').length > 0){
				var id = $('.chat-list-item.active').data('id') ;
				loadURL('/agent/agentusers.html?newuser=true&userid='+id , '#agentusers');
			}else{
				location.href = "/agent/index.html?newuser=true" ;
			}
		}
		if(data.userid == cursession){
			$('#agentuser-curstatus').remove();
			$("#chat_msg_list").append(template($('#begin_tpl').html(), {data: data}));
		}
	},
	newAgentUserMessage:function(data){
		if(data.usession == cursession){
			if(data.type == 'writing' && $('#writing').length > 0){
				$('#writing').remove();		
			}
			var id = $('.chat-list-item.active').data('id') ;
			if(data.message!=""){
				$("#chat_msg_list").append(template($('#message_tpl').html(), {data: data}));					
				document.getElementById('chat_msg_list').scrollTop = document.getElementById('chat_msg_list').scrollHeight  ;
			}
			loadURL("/agent/readmsg.html?userid="+data.agentuser);	//更新数据状态，将当前对话的新消息数量清空
		}else{
			if(data.type == 'message'){
				$('#last_msg_'+data.userid).text(data.tokenum).show();
				Proxy.addTopMsgTip(1) ;
			}
		}
	},
	endAgentUserService:function(data){
		if($('#tip_message_'+data.userid).length >0){
			$('#tip_message_'+data.userid).removeClass("bg-green").addClass('bg-gray').text('离开');
		}
		if(data.userid == cursession){
			$('#agentuser-curstatus').remove();
			$("#chat_msg_list").append(template($('#end_tpl').html(), {data: data}));
		}
	},
	tipMsgForm : function(href){
		top.layer.prompt({formType: 2,title: '请输入拉黑原因',area: ['300px', '50px']} , function(value, index, elem){
			location.href = href+"&description="+encodeURIComponent(value);
			top.layer.close(index);
		});
	},
	execContactsFunction: function(data){
		if(data!=null && data!= ""){
			if(typeof ani != "undefined"){
				loadURL("/apps/softphone/search.html?display=false&ani="+ani+"&q="+data, "#ukefu-chat-agent") ;
			}else if(userid && userid != '' && agentserviceid && agentserviceid != '' && agentuserid && agentuserid != ''){
				loadURL("/agent/contacts.html?userid="+userid+"&agentserviceid="+agentserviceid+"&agentuserid="+agentuserid+"&contactsid="+data , "#ukefu_contacts_info") ;
			}
		}
	},
	updateData : function(inner , data){
		$(inner).empty().append(data) ;
	},
	updateFormData : function(inner , data){
		$(inner).val(data).click() ;
	},
	addTopMsgTip : function(num){
		var msgNum = top.$('#ukefu-last-msg').data("num");
		msgNum = msgNum + num ;
		if(msgNum > 0){
			top.$('#ukefu-last-msg').data("num" , msgNum).show();
		}
		top.$('#msgnum').text(msgNum);
	},
	cleanTopMsgTip : function(num){
		var msgNum = top.$('#ukefu-last-msg').data("num");
		msgNum = msgNum - num ;
		if(msgNum > 0){
			top.$('#ukefu-last-msg').data("num" , msgNum).show();
			top.$('#msgnum').text(msgNum);
		}else{
			top.$('#ukefu-last-msg').data("num" , 0).hide();
			top.$('#msgnum').text(0);
		}
	}
}
var active = {
	tabAdd : function(href, title, id , reload) {
		//新增一个Tab项
		var layelement = layui.element;
		if ($('#' + id).length == 0) {
			layelement.tabAdd('ukefutab', {
				title : title //用于演示
				,
				content : '<iframe frameborder="0" src="' + href + '" id="'
						+ id + '" name="' + id
						+ '" width="100%" height="100%"></iframe>',
				id : id
			});
		}
		layelement.tabChange('ukefutab', id);
		$(".layui-this").each(function() {
			if (!$(this).parent().hasClass("layui-tab-title")) {
				$(this).removeClass("layui-this");
			}
		});
		if(reload == null){
			$('#' + id).attr("src", href);
		}
	},
	tabChange : function(href, title, id) {
		var layelement = layui.element;
		var inx = $('#' + id).parent().index();
		if ($('#' + id).length > 0) {
			$('#' + id).attr('src', href);
			layelement.tabChange('ukefutab', id);
		}
		$(".layui-this").each(function() {
			if (!$(this).parent().hasClass("layui-tab-title")) {
				$(this).removeClass("layui-this");
			}

		});
	}
};