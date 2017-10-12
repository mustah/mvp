import {createEmptyAction} from 'react-redux-typescript';

export const TOGGLE_CLUSTER_DIALOG = 'TOGGLE_CLUSTER_DIALOG';

export const toggleClusterDialog = createEmptyAction<string>(TOGGLE_CLUSTER_DIALOG);
