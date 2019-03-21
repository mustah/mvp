import {default as classNames} from 'classnames';
import {Dialog as MaterialDialog} from 'material-ui';
import * as React from 'react';
import {Children, OnClick} from '../../types/Types';
import DialogAction = __MaterialUI.DialogAction;

export interface DialogProps {
  actions?: Array<DialogAction | React.ReactElement<any>>;
  autoScrollBodyContent?: boolean;
  children?: Children;
  close: OnClick;
  contentClassName?: string;
  isOpen: boolean;
}

export const Dialog =
  ({actions, children, contentClassName, isOpen, close, autoScrollBodyContent}: DialogProps) => (
    <MaterialDialog
      className="Dialog-root"
      contentClassName={classNames('Dialog', contentClassName)}
      actions={actions}
      autoScrollBodyContent={autoScrollBodyContent || true}
      onRequestClose={close}
      open={isOpen}
    >
      {children}
    </MaterialDialog>
  );
