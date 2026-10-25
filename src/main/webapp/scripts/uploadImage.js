/* Image picker of the upload dialog: preview, remove, limits. No HTML string building. */
var ImageUpload = (function() {

	var MAX_SIZE = 5 * 1024 * 1024;
	var ALLOWED_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/bmp'];

	var selected = [];       // { file, url }
	var input = null, wrap = null, notify = function(msg) { alert(msg); };

	var render = function() {
		wrap.textContent = '';
		selected.forEach(function(item, index) {
			var box = document.createElement('div');
			box.className = 'upload__img-box';

			var img = document.createElement('img');
			img.src = item.url;
			img.alt = item.file.name;
			box.appendChild(img);

			var remove = document.createElement('button');
			remove.type = 'button';
			remove.className = 'upload__img-close';
			remove.setAttribute('aria-label', 'Remove ' + item.file.name);
			remove.addEventListener('click', function() {
				URL.revokeObjectURL(item.url);
				selected.splice(index, 1);
				render();
			});
			box.appendChild(remove);

			wrap.appendChild(box);
		});
	};

	var add = function(files) {
		var maxLength = parseInt(input.getAttribute('data-max_length'), 10) || 5;
		Array.prototype.forEach.call(files, function(file) {
			if (ALLOWED_TYPES.indexOf(file.type) === -1) {
				notify('Only PNG, JPEG, GIF and BMP images are allowed.');
				return;
			}
			if (file.size > MAX_SIZE) {
				notify('Image should be maximum 5 MB.');
				return;
			}
			if (selected.length >= maxLength) {
				notify('Maximum ' + maxLength + ' images allowed.');
				return;
			}
			selected.push({ file: file, url: URL.createObjectURL(file) });
		});
		input.value = '';
		render();
	};

	return {
		init: function(options) {
			input = document.querySelector('.upload__inputfile');
			wrap = document.querySelector('.upload__img-wrap');
			if (options && options.notify) notify = options.notify;
			input.addEventListener('change', function(e) { add(e.target.files); });
		},
		files: function() {
			return selected.map(function(item) { return item.file; });
		},
		clear: function() {
			selected.forEach(function(item) { URL.revokeObjectURL(item.url); });
			selected = [];
			render();
		}
	};
})();
