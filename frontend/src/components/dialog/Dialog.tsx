import {Dialog as MaterialDialog} from 'material-ui';
import * as React from 'react';
import {Children, OnClick} from '../../types/Types';
import {ButtonClose} from '../buttons/DialogButtons';

interface Props {
  children: Children;
  isOpen: boolean;
  autoScrollBodyContent: boolean;
  close: OnClick;
}

export const Dialog = ({children, isOpen, close, autoScrollBodyContent}: Props) => {
  const actions = [(<ButtonClose onClick={close}/>)];
  return (
    <MaterialDialog
      className="Dialog-root"
      contentClassName="Dialog"
      actions={actions}
      autoScrollBodyContent={autoScrollBodyContent}
      onRequestClose={close}
      open={isOpen}
    >
      {children}
    </MaterialDialog>
  );
};
