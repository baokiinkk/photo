package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import java.util.Date


class Photo : Parcelable {
    var id: Long? = null
    var displayName: String? = null
    var dateAdded: Date? = null
    var contentUri: Uri? = null
    var timesPick: Int = 0
    var timeMf: Long = 0

    var maskPath: String? = null
    var template: String? = null

    //Primary info
    //Using point list to construct view. All points and width, height are in [0, 1] range.
    var x: Float = 0f
    var y: Float = 0f
    var pointList: ArrayList<PointF?>? = ArrayList<PointF?>()
    var bound: RectF? = RectF()

    //Using pathPhoto to create
    var path: Path? = null
    var pathRatioBound: RectF? = null
    var pathInCenterHorizontal: Boolean = false
    var pathInCenterVertical: Boolean = false
    var pathAlignParentRight: Boolean = false
    var pathScaleRatio: Float = 1f
    var fitBound: Boolean = false

    //other info
    var hasBackground: Boolean = false
    var shrinkMethod: Int = SHRINK_METHOD_DEFAULT
    var cornerMethod: Int = CORNER_METHOD_DEFAULT
    var disableShrink: Boolean = false
    var shrinkMap: HashMap<PointF?, PointF?>? = null

    //Clear polygon or arc area
    var clearAreaPoints: ArrayList<PointF?>? = null

    //Clear an area using pathPhoto
    var clearPath: Path? = null
    var clearPathRatioBound: RectF? = null
    var clearPathInCenterHorizontal: Boolean = false
    var clearPathInCenterVertical: Boolean = false
    var clearPathScaleRatio: Float = 1f
    var centerInClearBound: Boolean = false

    constructor()

    constructor(id: Long?, displayName: String?, dateAdded: Date?, contentUri: Uri?) {
        this.id = id
        this.displayName = displayName
        this.dateAdded = dateAdded
        this.contentUri = contentUri
    }

    constructor(contentUri: Uri?, timesPick: Int) {
        this.contentUri = contentUri
        this.timesPick = timesPick
    }

    constructor(path: String?, timePick: Int) : this(Uri.parse(path), timePick)


    protected constructor(`in`: Parcel) {
        if (`in`.readByte().toInt() == 0) {
            id = null
        } else {
            id = `in`.readLong()
        }
        displayName = `in`.readString()
        contentUri = `in`.readParcelable<Uri?>(Uri::class.java.getClassLoader())
        timesPick = `in`.readInt()
        timeMf = `in`.readLong()
        maskPath = `in`.readString()
        template = `in`.readString()
        x = `in`.readFloat()
        y = `in`.readFloat()
        pointList = `in`.createTypedArrayList<PointF?>(PointF.CREATOR)
        bound = `in`.readParcelable<RectF?>(RectF::class.java.getClassLoader())
        pathRatioBound = `in`.readParcelable<RectF?>(RectF::class.java.getClassLoader())
        pathInCenterHorizontal = `in`.readByte().toInt() != 0
        pathInCenterVertical = `in`.readByte().toInt() != 0
        pathAlignParentRight = `in`.readByte().toInt() != 0
        pathScaleRatio = `in`.readFloat()
        fitBound = `in`.readByte().toInt() != 0
        hasBackground = `in`.readByte().toInt() != 0
        shrinkMethod = `in`.readInt()
        cornerMethod = `in`.readInt()
        disableShrink = `in`.readByte().toInt() != 0
        clearAreaPoints = `in`.createTypedArrayList<PointF?>(PointF.CREATOR)
        clearPathRatioBound = `in`.readParcelable<RectF?>(RectF::class.java.getClassLoader())
        clearPathInCenterHorizontal = `in`.readByte().toInt() != 0
        clearPathInCenterVertical = `in`.readByte().toInt() != 0
        clearPathScaleRatio = `in`.readFloat()
        centerInClearBound = `in`.readByte().toInt() != 0
    }

    fun clearPoint() {
        if (pointList != null && pointList!!.size > 0) pointList!!.clear()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (id == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(id!!)
        }
        dest.writeString(displayName)
        dest.writeParcelable(contentUri, flags)
        dest.writeInt(timesPick)
        dest.writeLong(timeMf)
        dest.writeString(maskPath)
        dest.writeString(template)
        dest.writeFloat(x)
        dest.writeFloat(y)
        dest.writeTypedList<PointF?>(pointList)
        dest.writeParcelable(bound, flags)
        dest.writeParcelable(pathRatioBound, flags)
        dest.writeByte((if (pathInCenterHorizontal) 1 else 0).toByte())
        dest.writeByte((if (pathInCenterVertical) 1 else 0).toByte())
        dest.writeByte((if (pathAlignParentRight) 1 else 0).toByte())
        dest.writeFloat(pathScaleRatio)
        dest.writeByte((if (fitBound) 1 else 0).toByte())
        dest.writeByte((if (hasBackground) 1 else 0).toByte())
        dest.writeInt(shrinkMethod)
        dest.writeInt(cornerMethod)
        dest.writeByte((if (disableShrink) 1 else 0).toByte())
        dest.writeTypedList<PointF?>(clearAreaPoints)
        dest.writeParcelable(clearPathRatioBound, flags)
        dest.writeByte((if (clearPathInCenterHorizontal) 1 else 0).toByte())
        dest.writeByte((if (clearPathInCenterVertical) 1 else 0).toByte())
        dest.writeFloat(clearPathScaleRatio)
        dest.writeByte((if (centerInClearBound) 1 else 0).toByte())
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj !is Photo) return false
        val photo = obj
        if (TextUtils.isEmpty(photo.displayName) || TextUtils.isEmpty(displayName)) return false
        return this.id == photo.id && this.displayName.equals(photo.displayName, ignoreCase = true)
    }

    companion object {
        private const val SHRINK_METHOD_DEFAULT = 0
        const val SHRINK_METHOD_3_3: Int = 1
        const val SHRINK_METHOD_USING_MAP: Int = 2
        const val SHRINK_METHOD_3_6: Int = 3
        const val SHRINK_METHOD_3_8: Int = 4
        const val SHRINK_METHOD_COMMON: Int = 5
        private const val CORNER_METHOD_DEFAULT = 0
        const val CORNER_METHOD_3_6: Int = 1
        const val CORNER_METHOD_3_13: Int = 2
        val CREATOR: Parcelable.Creator<Photo?> = object : Parcelable.Creator<Photo?> {
            override fun createFromParcel(`in`: Parcel): Photo {
                return Photo(`in`)
            }

            override fun newArray(size: Int): Array<Photo?> {
                return arrayOfNulls<Photo>(size)
            }
        }
    }
}