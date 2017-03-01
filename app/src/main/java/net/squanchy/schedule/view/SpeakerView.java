package net.squanchy.schedule.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.squanchy.R;
import net.squanchy.imageloader.ImageLoader;
import net.squanchy.imageloader.ImageLoaderInjector;
import net.squanchy.search.speaker.view.Speaker;
import net.squanchy.support.lang.Lists;

public class SpeakerView extends LinearLayout {

    private final LayoutInflater layoutInflater;

    @Nullable
    private ImageLoader imageLoader;

    private ViewGroup speakerPhotoContainer;
    private TextView speakerNameView;

    public SpeakerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeakerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SpeakerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (!isInEditMode()) {
            imageLoader = ImageLoaderInjector.obtain(context).imageLoader();
        }
        super.setOrientation(VERTICAL);

        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void setOrientation(int orientation) {
        throw new UnsupportedOperationException("SpeakerView doesn't support changing orientation");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        speakerPhotoContainer = (ViewGroup) findViewById(R.id.speaker_photos_container);
        speakerNameView = (TextView) findViewById(R.id.speaker_names);
    }

    public void updateWith(List<Speaker> speakers) {
        speakerNameView.setText(toCommaSeparatedNames(speakers));

        updateSpeakerPhotos(speakers);
    }

    private String toCommaSeparatedNames(List<Speaker> speakers) {
        StringBuilder builder = Lists.reduce(new StringBuilder(), speakers, (sb, speaker) -> sb.append(speaker.fullName()).append(", "));
        int stringLength = builder.length();
        return builder.delete(stringLength - 2, stringLength).toString();
    }

    private void updateSpeakerPhotos(List<Speaker> speakers) {
        if (imageLoader == null) {
            throw new IllegalStateException("Unable to access the ImageLoader, it hasn't been initialized yet");
        }

        List<ImageView> photoViews;
        if (speakerPhotoContainer.getChildCount() > 0) {
            photoViews = getAllImageViewsContainedIn(speakerPhotoContainer);
            speakerPhotoContainer.removeAllViews();
        } else {
            photoViews = Collections.emptyList();
        }

        for (Speaker speaker : speakers) {
            ImageView photoView = recycleOrInflatePhotoView(photoViews);
            speakerPhotoContainer.addView(photoView);
            loadSpeakerPhoto(photoView, speaker.avatarImageURL(), imageLoader);
        }
    }

    private ImageView recycleOrInflatePhotoView(List<ImageView> photoViews) {
        if (photoViews.isEmpty()) {
            return inflatePhotoView();
        } else {
            return photoViews.remove(0);
        }
    }

    private ImageView inflatePhotoView() {
        return (ImageView) layoutInflater.inflate(R.layout.view_speaker_photo_small, speakerPhotoContainer, false);
    }

    private void loadSpeakerPhoto(ImageView photoView, String photoUrl, ImageLoader imageLoader) {
        StorageReference photoReference = FirebaseStorage.getInstance().getReference(photoUrl);
        imageLoader.load(photoReference).into(photoView);
    }

    private List<ImageView> getAllImageViewsContainedIn(ViewGroup container) {
        int childCount = container.getChildCount();
        List<ImageView> children = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            View child = container.getChildAt(i);
            children.add((ImageView) child);
        }
        return children;
    }
}
