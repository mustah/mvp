import * as classNames from 'classnames';
import {Dialog as MaterialDialog} from 'material-ui';
import * as React from 'react';
import {Children, OnClick} from '../../types/Types';
import {ButtonClose} from '../buttons/DialogButtons';
import DialogAction = __MaterialUI.DialogAction;

interface Props {
  children: Children;
  isOpen: boolean;
  autoScrollBodyContent: boolean;
  close: OnClick;
  actions?: Array<DialogAction | React.ReactElement<any>>;
  contentClassName?: string;
}

export const Dialog = ({actions, children, contentClassName, isOpen, close, autoScrollBodyContent}: Props) => {
  const listOfActions = actions || [(<ButtonClose onClick={close} key="close"/>)];
  return (
    <MaterialDialog
      className="Dialog-root"
      contentClassName={classNames('Dialog', contentClassName)}
      actions={listOfActions}
      autoScrollBodyContent={autoScrollBodyContent}
      onRequestClose={close}
      open={isOpen}
    >
      {children}
    </MaterialDialog>
  );
};
