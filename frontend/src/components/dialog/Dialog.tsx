import {Dialog as MaterialDialog} from 'material-ui';
import * as React from 'react';
import {Children, OnClick} from '../../types/Types';
import {ButtonClose} from '../buttons/ButtonClose';

export interface Props {
  children: Children;
  isOpen: boolean;
  close: OnClick;
}

export const Dialog = (props: Props) => {
  const {children, isOpen, close} = props;

  return (
    <MaterialDialog
      contentClassName="Dialog"
      actions={[(<ButtonClose onClick={close}/>)]}
      autoScrollBodyContent={true}
      onRequestClose={close}
      open={isOpen}
    >
      {children}
    </MaterialDialog>
  );
};
