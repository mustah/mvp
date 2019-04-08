import * as React from 'react';
import {CallbackWithId, Identifiable, OnClick, OnClickWithId, Opened, uuid} from '../../types/Types';

type State = Opened & Partial<Identifiable>;

interface ConfirmDialogHook extends State {
  closeConfirm: OnClick;
  openConfirm: OnClickWithId;
  confirm: OnClick;
}

export const useConfirmDialog = (onConfirm: CallbackWithId): ConfirmDialogHook => {
  const [{isOpen, id}, setOpened] = React.useState<State>({isOpen: false});
  const closeConfirm = () => setOpened({isOpen: false});
  const openConfirm = (id: uuid) => setOpened({isOpen: true, id});
  const confirm = () => onConfirm(id!);

  return {
    closeConfirm,
    confirm,
    isOpen,
    id,
    openConfirm,
  };
};
