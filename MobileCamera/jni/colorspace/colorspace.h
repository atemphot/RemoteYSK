/*****************************************************************************
 *
 *  XVID MPEG-4 VIDEO CODEC
 *  - Colorspace related header  -
 *
 *  Copyright(C) 2001-2003 Peter Ross <pross@xvid.org>
 *
 *  This program is free software ; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation ; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY ; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program ; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 *
 * $Id$
 *
 ****************************************************************************/

#ifndef _COLORSPACE_H
#define _COLORSPACE_H



typedef signed char __s8;
typedef unsigned char __u8;

typedef short __s16;
typedef unsigned short __u16;

typedef int __s32;
typedef unsigned int __u32;

typedef long long __s64;
typedef unsigned long long __u64;


typedef		__u8		uint8_t;
typedef		__u8		u_int8_t;
typedef		__s8		int8_t;

typedef		__u16		uint16_t;
typedef		__u16		u_int16_t;
typedef		__s16		int16_t;

typedef		__u32		uint32_t;
typedef		__u32		u_int32_t;
typedef		__s32		int32_t;

typedef		__u64		uint64_t;
typedef		__u64		u_int64_t;
typedef		__s64		int64_t;


/*
 * min()/max() macros that also do
 * strict type-checking.. See the
 * "unnecessary" pointer comparison.
 */
#define MIN(x,y)  ((x) > (y) ? (y) : (x))

#define MAX(x,y)  ((x) < (y) ? (y) : (x))




/* initialize tables */

void colorspace_init(void);



/* colorspace conversion function (encoder) */

typedef void (packedFunc) (uint8_t * x_ptr,
								 int x_stride,
								 uint8_t * y_src,
								 uint8_t * v_src,
								 uint8_t * u_src,
								 int y_stride,
								 int uv_stride,
								 int width,
								 int height,
								 int vflip);

typedef packedFunc *packedFuncPtr;


/* plain c */
packedFunc rgb555_to_yv12_c;
packedFunc rgb565_to_yv12_c;
packedFunc bgr_to_yv12_c;
packedFunc bgra_to_yv12_c;
packedFunc abgr_to_yv12_c;
packedFunc rgba_to_yv12_c;
packedFunc argb_to_yv12_c;
packedFunc yuyv_to_yv12_c;
packedFunc uyvy_to_yv12_c;

packedFunc rgb555i_to_yv12_c;
packedFunc rgb565i_to_yv12_c;
packedFunc bgri_to_yv12_c;
packedFunc bgrai_to_yv12_c;
packedFunc abgri_to_yv12_c;
packedFunc rgbai_to_yv12_c;
packedFunc argbi_to_yv12_c;
packedFunc yuyvi_to_yv12_c;
packedFunc uyvyi_to_yv12_c;


/* plain c */
packedFunc yv12_to_rgb555_c;
packedFunc yv12_to_rgb565_c;
packedFunc yv12_to_bgr_c;
packedFunc yv12_to_bgra_c;
packedFunc yv12_to_abgr_c;
packedFunc yv12_to_rgba_c;
packedFunc yv12_to_argb_c;
packedFunc yv12_to_yuyv_c;
packedFunc yv12_to_uyvy_c;

packedFunc yv12_to_rgb555i_c;
packedFunc yv12_to_rgb565i_c;
packedFunc yv12_to_bgri_c;
packedFunc yv12_to_bgrai_c;
packedFunc yv12_to_abgri_c;
packedFunc yv12_to_rgbai_c;
packedFunc yv12_to_argbi_c;
packedFunc yv12_to_yuyvi_c;
packedFunc yv12_to_uyvyi_c;


typedef void (planarFunc) (
				uint8_t * y_dst, uint8_t * u_dst, uint8_t * v_dst,
				int y_dst_stride, int uv_dst_stride,
				uint8_t * y_src, uint8_t * u_src, uint8_t * v_src,
				int y_src_stride, int uv_src_stride,
				int width, int height, int vflip);

typedef planarFunc *planarFuncPtr;


planarFunc yv12_to_yv12_c;


#endif							/* _COLORSPACE_H */
