import * as React from 'react';
import {GatewayDetailsContainer} from '../../containers/dialogs/GatewayDetailsContainer';
import {MeterDetailsContainer} from '../../containers/dialogs/MeterDetailsContainer';
import {SelectedId} from '../../usecases/map/mapModels';
import {testOrNull} from '../hoc/hocs';
import {Dialog, DialogProps} from './Dialog';

export type DetailsDialogProps = SelectedId & DialogProps;

const DialogOrNull = testOrNull<DetailsDialogProps>(
  ({selectedId}: DetailsDialogProps) => selectedId.isJust(),
)(Dialog);

const DetailsDialog = (props: DetailsDialogProps) => (
  <DialogOrNull {...props}>
    {props.children}
  </DialogOrNull>
);

export const MeterDetailsDialog = (props: DetailsDialogProps) => (
  <DetailsDialog {...props}>
    <MeterDetailsContainer selectedId={props.selectedId}/>
  </DetailsDialog>
);

export const GatewayDetailsDialog = (props: DetailsDialogProps) => (
  <DetailsDialog {...props}>
    <GatewayDetailsContainer selectedId={props.selectedId}/>
  </DetailsDialog>
);
