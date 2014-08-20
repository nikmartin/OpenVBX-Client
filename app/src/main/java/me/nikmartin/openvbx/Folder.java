package me.nikmartin.openvbx;

public class Folder {
	private int id;
	private String name = null;
	private String type = null;
	private int unread = 0;

   public int getCount() {
      return count;
   }

   private int count = 0;

	public int getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public String getType() {
		return type;
	}

	public int getUnread() {
		return unread;
	}

	public Folder(int id, String name, String type, int count, int unread) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.count = count;
      this.unread = unread;

	}
}
