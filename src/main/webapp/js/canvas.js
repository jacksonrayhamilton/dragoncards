/*global imageSrcs*/

var canvas = document.getElementById('canvas');
var ctx = canvas.getContext('2d');

var blueDragonImage = new Image();
blueDragonImage.src = imageSrcs.blueDragon;

function clear() {
  // Store the current transformation matrix
  ctx.save();

  // Use the identity matrix while clearing the canvas
  ctx.setTransform(1, 0, 0, 1, 0, 0);
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Restore the transform
  ctx.restore();
}

function Sprite(img, x, y, w, h, dx, dy) {
  this.img = img;
  this.x = x;
  this.y = y;
  this.w = w;
  this.h = h;
  this.dx = dx;
  this.dy = dy;
}

Sprite.prototype.draw = function (ctx, x, y) {
  ctx.drawImage(this.img, this.x, this.y, this.w, this.h, this.dx + x, this.dy + y, this.w, this.h);
};

function BlueDragonSprite(x, y) {
  this.x = x;
  this.y = y;
  this.headDx = 0;
  this.headDy = 0;
  this.backDx = 0;
  this.backDy = 0;
}

BlueDragonSprite.prototype.head = new Sprite(blueDragonImage, 58, 83 + 2, 35, 57, 9, 14);
BlueDragonSprite.prototype.back = new Sprite(blueDragonImage, 0, 84, 41, 42, 29, 0);
BlueDragonSprite.prototype.body = new Sprite(blueDragonImage, 0, 0, 97, 82, 0, 16);
BlueDragonSprite.prototype.draw = function (ctx) {
  this.body.draw(ctx, this.x, this.y);
  this.back.draw(ctx, this.x + this.backDx, this.y + this.backDy);
  this.head.draw(ctx, this.x + this.headDx, this.y + this.headDy);
};
BlueDragonSprite.prototype.animate = function (ctx) {
  var xInitial = this.x;
  var xDisplacement = 0;
  var formMaxDisplacement = 4;
  var headMaxDisplacement = 1;
  var backMaxDisplacement = 0;
  var formDirection = 1;
  var headDirection = 1;
  var backDirection = 1;
  window.setInterval(function () {
    if (Math.abs(xDisplacement) >= formMaxDisplacement) {
      formDirection = -1 * formDirection;
    }
    xDisplacement += 1 * formDirection;
    this.x = xInitial + xDisplacement;

    if (Math.abs(this.headDx) >= headMaxDisplacement) {
      headDirection = -1 * headDirection;
    }
    this.headDx += 1 * headDirection;

    if (Math.abs(this.backDy) >= backMaxDisplacement) {
      backDirection = -1 * backDirection;
    }
    this.backDy += 1 * backDirection;

    clear();
    this.draw(ctx);
  }.bind(this), 200);
};

var blueDragonSprite = new BlueDragonSprite(5, 5);
blueDragonSprite.animate(ctx);
