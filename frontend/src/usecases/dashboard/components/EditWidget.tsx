import {cloneDeep} from 'lodash';
import * as React from 'react';
import {ButtonCancel, ButtonConfirm} from '../../../components/buttons/DialogButtons';
import {Dialog} from '../../../components/dialog/Dialog';
import {SelectFieldInput} from '../../../components/inputs/InputSelectable';
import {MainTitle} from '../../../components/texts/Titles';
import {firstUpperTranslated} from '../../../services/translationService';
import {Widget} from '../../../state/domain-models/widget/widgetModels';
import {initialSelectionId} from '../../../state/user-selection/userSelectionModels';
import {CallbackWith, IdNamed, OnClick, uuid} from '../../../types/Types';
import {StateToProps} from '../containers/EditWidgetContainer';
import './EditWidget.scss';

interface OwnProps<T extends Widget> {
  isOpen: boolean;
  onCancel: OnClick;
  onSave: CallbackWith<T>;
  id: uuid;
  dashboardId: uuid;
  widgets: T;
}

type Props<T extends Widget> = StateToProps & OwnProps<T>;

export const EditWidget = <T extends Widget>({
  userSelections,
  isOpen,
  onCancel,
  onSave,
  widgets,
}: Props<T>) => {
  const selectionOptions: IdNamed[] = [
    {id: initialSelectionId, name: firstUpperTranslated('all meters')},
    ...userSelections.result.map((id: uuid) => ({id, name: userSelections.entities[id].name}))
  ];
  const selectionLabel = firstUpperTranslated('selection');
  const [selectionId, selectSelection] = React.useState<uuid | undefined>(widgets.settings.selectionId);

  const onChange = (_, __, value) => selectSelection(value);

  const selectedSelection: uuid | undefined = selectionId || selectionOptions[0].id;

  const save = () => {
    const widget: T = cloneDeep(widgets);
    if (selectionId === initialSelectionId) {
      delete widget.settings.selectionId;
    } else {
      widget.settings.selectionId = selectionId;
    }
    onSave(widget);
  };

  const actions = [
    <ButtonCancel onClick={onCancel} key="cancel"/>,
    <ButtonConfirm onClick={save} key="confirm" disabled={!selectedSelection}/>,
  ];

  return (
    <Dialog
      close={onCancel}
      isOpen={isOpen}
      actions={actions}
      contentClassName="widget-edit"
    >
      <MainTitle>{firstUpperTranslated('widget settings')}</MainTitle>
      <SelectFieldInput
        options={selectionOptions}
        floatingLabelText={selectionLabel}
        hintText={selectionLabel}
        id="selectionId"
        multiple={false}
        onChange={onChange}
        value={selectedSelection}
      />
    </Dialog>
  );
};
