import * as React from 'react';
import {GatewayDetailsContainer} from '../../containers/dialogs/GatewayDetailsContainer';
import {MeterDetailsContainer} from '../../containers/dialogs/MeterDetailsContainer';
import {SelectedId} from '../../usecases/map/mapModels';
import {ButtonClose} from '../buttons/DialogButtons';
import {componentOrNothing} from '../hoc/hocs';
import {Dialog, DialogProps} from './Dialog';

export type DetailsDialogProps = SelectedId & DialogProps;

const DialogComponent = componentOrNothing<DetailsDialogProps>(({selectedId}) => selectedId.isJust())(Dialog);

const DetailsDialog = (props: DetailsDialogProps) => (
  <DialogComponent {...props}>
    <ButtonClose onClick={props.close}/>
    {props.children}
  </DialogComponent>
);

export const MeterDetailsDialog = (props: DetailsDialogProps) => (
  <DetailsDialog {...props}>
    <MeterDetailsContainer selectedId={props.selectedId} useCollectionPeriod={false}/>
  </DetailsDialog>
);

export const GatewayDetailsDialog = (props: DetailsDialogProps) => (
  <DetailsDialog {...props}>
    <GatewayDetailsContainer selectedId={props.selectedId}/>
  </DetailsDialog>
);
