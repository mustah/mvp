import {Dialog as MaterialDialog} from 'material-ui';
import * as React from 'react';
import {firstUpperTranslated} from '../../services/translationService';
import {OnClick} from '../../types/Types';
import {ButtonCancel, ButtonConfirm} from '../buttons/DialogButtons';

interface Props {
  isOpen: boolean;
  confirm: OnClick;
  close: OnClick;
  text?: string;
}

export const ConfirmDialog = ({
  isOpen,
  close,
  confirm,
  text = firstUpperTranslated('are you sure you want to delete this item')
}: Props) => {
  const confirmAndClose = () => {
    confirm();
    close();
  };
  const actions = [
    <ButtonCancel onClick={close} key="cancel"/>,
    <ButtonConfirm onClick={confirmAndClose} key="confirm"/>,
  ];

  return (
    <MaterialDialog
      actions={actions}
      autoScrollBodyContent={true}
      onRequestClose={close}
      open={isOpen}
    >
      {`${text}?`}
    </MaterialDialog>
  );
};
