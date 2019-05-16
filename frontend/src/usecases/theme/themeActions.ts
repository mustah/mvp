import {createAction, createStandardAction} from 'typesafe-actions';
import {Color} from '../../app/colors';

export const changePrimaryColor = createStandardAction('CHANGE_PRIMARY_COLOR')<Color>();

export const changeSecondaryColor = createStandardAction('CHANGE_SECONDARY_COLOR')<Color>();

export const resetColors = createAction('RESET_COLORS');
