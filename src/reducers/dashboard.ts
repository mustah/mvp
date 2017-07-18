import {AnyAction} from 'redux';
import {Selectable} from '../types/Types';

export interface DashboardProps extends Selectable {
  title?: string;
}

const initialState = {isSelected: false};

export const dashboard = (state: DashboardProps = initialState, action: AnyAction): DashboardProps => {
  switch (action.type) {
    default:
      return state;
  }
};
