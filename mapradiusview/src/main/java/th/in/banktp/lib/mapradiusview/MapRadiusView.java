package th.in.banktp.lib.mapradiusview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by bank on 11/28/2016 AD.
 */

public class MapRadiusView extends FrameLayout {
    private int animDuration = 300;
    private int currentRadius = 6000;
    private int currentZoomLevel = 1;


    private GoogleMap _map;
    private double _metersPerPixel;
    private int _minWidth;
    private ImageView _ivRadius;
    private ImageView _ivPin;
    private ValueAnimator _valueAnim;
    private FrameLayout.LayoutParams _lpRadius;
    private int _animStartSize, _animWidthMargin;


    public MapRadiusView(Context context) {
        super(context);
        initView();
    }

    public MapRadiusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MapRadiusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MapRadiusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }


    private void initView() {
        // get custom properties

        // init variable
        _valueAnim = ValueAnimator.ofFloat(0, 1);
        _valueAnim.setDuration(animDuration);
        _valueAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float v = (float) valueAnimator.getAnimatedValue();
                int newSize = (int) (_animStartSize + (v * _animWidthMargin));
                _lpRadius.width = newSize;
                _lpRadius.height = newSize;
                _ivRadius.setLayoutParams(_lpRadius);
            }
        });
        // inflate layout
        LayoutInflater.from(getContext()).inflate(R.layout.mapradiusview_layout, this, true);
        _ivRadius = (ImageView) findViewById(R.id.mapradiusview_radius);
        _ivPin = (ImageView) findViewById(R.id.mapradiusview_pin);

        _ivRadius.post(new Runnable() {
            @Override
            public void run() {
                _minWidth = Math.min(getWidth(), getHeight());
                _lpRadius = (LayoutParams) _ivRadius.getLayoutParams();
                setRadius(currentRadius);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        this._minWidth = Math.min(width, height);
    }

    private void mapReady(GoogleMap googleMap) {
        MapRadiusView.this._map = googleMap;

        // config maap
        _map.getUiSettings().setAllGesturesEnabled(false);
    }


    /**
     * at zoom level 1
     * meters pixel = equatorLength/256
     * +1 level will be previous level *2
     * at zoom level 2 will be equatorLength / (256*2)
     * so that at zoom18 meter per pixels is equatorLength / ( 256 * 2^17 )
     * <p>
     * http://stackoverflow.com/a/6452332
     */
    private final int maxZoom = 18;
    private final double maxZoomMetersPerPixel = 6378140 / (256 * Math.pow(2, maxZoom - 1));

    private void calculateZoomLevelAndMeterPerPixel(int radiusMeter) {
        radiusMeter *= 2;// make it circle length;
        double metersPerPixel = maxZoomMetersPerPixel;
        int zoomLevel = maxZoom;
        while ((metersPerPixel * _minWidth) < radiusMeter) {
            metersPerPixel *= 2;
            --zoomLevel;
        }
        this._metersPerPixel = metersPerPixel;
        currentZoomLevel = zoomLevel;
    }

    private void animateCircle() {
        // calculate circle
        int radiusPixel = (int) (currentRadius / _metersPerPixel);
        _valueAnim.cancel(); // cancel before start new
        _animStartSize = _ivRadius.getWidth();
        _animWidthMargin = (radiusPixel * 2) - _animStartSize;
        _valueAnim.start();
    }


    /**
     * Initialize view
     *
     * @param fragmentManager
     */
    public void init(FragmentManager fragmentManager) {
        init(fragmentManager, null);
    }


    /**
     * Initialize view
     *
     * @param fragmentManager    support fragment manager
     * @param onMapReadyCallback on map ready callback
     */
    public void init(FragmentManager fragmentManager, final OnMapReadyCallback onMapReadyCallback) {
        init(fragmentManager, null, currentRadius, onMapReadyCallback);
    }

    /**
     * @param fragmentManager
     * @param center
     * @param radius
     */
    public void init(FragmentManager fragmentManager, final LatLng center, int radius) {
        init(fragmentManager, center, radius, null);
    }

    /**
     * Initialize view
     *
     * @param fragmentManager    support fragment manager
     * @param center             set center of map when map is ready
     * @param onMapReadyCallback on map ready callback
     */
    public void init(FragmentManager fragmentManager, final LatLng center, int radius, final OnMapReadyCallback onMapReadyCallback) {
        this.currentRadius = radius;
        ((SupportMapFragment) fragmentManager.findFragmentById(R.id.mapradiusview_map))
                .getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        if (center != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(center));
                        }
                        mapReady(googleMap);
                        if (onMapReadyCallback != null)
                            onMapReadyCallback.onMapReady(googleMap);

                        if (_lpRadius != null && _lpRadius.width == 0) {
                            setRadius(currentRadius);
                        }
                    }
                });
    }

    /**
     * set animation duration of both map and distance radius
     *
     * @param milli duration in miliseconds
     */
    public void setAnimateDuration(int milli) {
        this.animDuration = milli;
        _valueAnim.setDuration(milli);
    }

    /**
     * animate both camera and distance radius with duration set with setAnimateDuration method
     *
     * @param meters radius in meter
     */
    public synchronized void setRadius(int meters) {
        if (_map == null)
            return;
        this.currentRadius = meters;
        calculateZoomLevelAndMeterPerPixel(currentRadius);
        _map.animateCamera(CameraUpdateFactory.newLatLngZoom(_map.getCameraPosition().target, currentZoomLevel), animDuration, null);
        animateCircle();
    }

}
