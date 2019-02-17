package cn.endureblaze.ka.resources;
import android.app.*;
import android.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import cn.endureblaze.ka.*;
import cn.endureblaze.ka.bean.*;
import cn.endureblaze.ka.utils.*;
import java.util.*;

import cn.endureblaze.ka.R;
import cn.endureblaze.ka.resources.game.*;
import cn.endureblaze.ka.main.*;
import com.bumptech.glide.*;

public class ConsoleAdapter extends RecyclerView.Adapter<ConsoleAdapter.ViewHolder>
{
    private Context mContext;
    private List<Console> mConsoleList;

	private Activity mActivity;
    static class ViewHolder extends RecyclerView.ViewHolder
	{
        LinearLayout LinearLayout;
		CardView cardView;
        ImageView consoleImage;
        TextView consoleName;
        public ViewHolder(View view)
		{
            super(view);
			LinearLayout = (LinearLayout)view.findViewById(R.id.LinearLayout);
            cardView = (CardView) view.findViewById(R.id.cardview);
			consoleImage = (ImageView) view.findViewById(R.id.console_image);
            consoleName = (TextView) view.findViewById(R.id.console_text);
        }
    }

    public ConsoleAdapter(List<Console> consolelist,Activity activity)
	{
        mConsoleList = consolelist;
		mActivity=activity;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
        if (mContext == null)
		{
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_console, parent, false);
		final ViewHolder holder=new ViewHolder(view);
		holder.LinearLayout.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v)
				{
					int position=holder.getAdapterPosition();
					Console console=mConsoleList.get(position);
					Intent in=new Intent(mContext, GameListActivity.class);
					String  input=console.getName().toString();	
					MainActivity m=new MainActivity();
					IntentUtil.startActivityWithAnim(in,mActivity);
					SharedPreferences.Editor t=mContext.getSharedPreferences("string", 0).edit();
					t.putString("主机名称", input);
					t.apply();
				}
			}
		);
		return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
	{
        Console co = mConsoleList.get(position);
        holder.consoleName.setText(co.getName());
        Glide
			.with(mContext)
			.load(co.getImageUrl())
			//.apply(Kirby.getGlideRequestOptions())
			.asBitmap()
		    .fitCenter()
			.placeholder(R.drawable.ic_kirby_download)
			.error(R.drawable.ic_kirby_load_fail)
			.into(holder.consoleImage);		
		}
    @Override
    public int getItemCount()
	{
        return mConsoleList.size();
    }
}