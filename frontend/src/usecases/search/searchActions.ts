import {createEmptyAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';

export const CLOSE_SEARCH = 'CLOSE_SEARCH';

const closeSearchAction = createEmptyAction(CLOSE_SEARCH);

export const closeSearch = () => dispatch => {
  dispatch(closeSearchAction());
  dispatch(routerActions.goBack());
};
