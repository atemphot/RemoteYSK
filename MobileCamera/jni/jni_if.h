#ifndef _JNI_IF_H_
#define _JNI_IF_H_


void if_contrl_turn_up_little();
void if_contrl_turn_down_little();

void if_contrl_move_advance_little(int param);
void if_contrl_move_back_little(int param);
void if_contrl_move_advance_left_little(int param);
void if_contrl_move_advance_right_little(int param);
void if_contrl_move_back_left_little(int param);
void if_contrl_move_back_right_little(int param);

void if_gc_arm();
void if_gc_disarm();
void if_gc_invalid();
void if_gc_detect_obj(int obj_type);


#endif /* _JNI_IF_H_ */
