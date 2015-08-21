package com.amazing.deepblue;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class InfoActivity  extends Activity 
{
	ImageButton backButton = null;
	TextView infoTextView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		initInfoTextView();
		
		backButton = (ImageButton)findViewById(R.id.back_button);
		backButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				finish();
			}
		});
	}
	
	private void initInfoTextView()
	{
		infoTextView = (TextView)findViewById(R.id.info_text_view);
		
		Typeface typeface = Typeface.createFromAsset(getAssets(), getString( R.string.font_path ));
		infoTextView.setTypeface(typeface);
	}
}












