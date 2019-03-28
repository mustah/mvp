import * as React from 'react';
import {connect} from 'react-redux';
import {cloneDeep} from 'lodash';
import {ButtonCancel, ButtonConfirm} from '../../../../components/buttons/DialogButtons';
import {Dialog} from '../../../../components/dialog/Dialog';
import {SelectFieldInput} from '../../../../components/inputs/InputSelectable';
import {MainTitle} from '../../../../components/texts/Titles';
import {RootState} from '../../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../../services/translationService';
import {NormalizedState} from '../../../../state/domain-models/domainModels';
import {UserSelection} from '../../../../state/user-selection/userSelectionModels';
import {Widget} from '../../../../state/domain-models/widget/widgetModels';
import {CallbackWith, IdNamed, OnClick, uuid} from '../../../../types/Types';
import './EditWidget.scss';

const ALL_METERS = -1;

const EditWidget = <T extends Widget>(props: Props<T>) => {
  const {
    userSelections,
    isOpen,
    onCancel,
    onSave,
    settings: {settings},
  } = props;

  const selectionOptions: IdNamed[] = [
    {id: ALL_METERS, name: firstUpperTranslated('all meters')},
    ...userSelections.result.map(
      (id: uuid) => ({
        id,
        name: userSelections.entities[id].name,
      })
    )
  ];
  const selectionLabel = firstUpperTranslated('selection');
  const [selectionId, selectSelection] = React.useState(settings.selectionId);

  const onChange = (event, index, value) => {
    selectSelection(value);
  };

  const selectedSelection: uuid | undefined = selectionId || selectionOptions[0].id;

  const save = () => {
    const widget: T = cloneDeep(props.settings);
    if (selectionId === ALL_METERS) {
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
      contentClassName={'widget-edit'}
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

type Props<T extends Widget> = StateToProps & OwnProps<T>;

interface OwnProps<T extends Widget> {
  isOpen: boolean;
  onCancel: OnClick;
  onSave: CallbackWith<T>;
  id: uuid;
  dashboardId: uuid;
  settings: T;
}

interface StateToProps {
  userSelections: NormalizedState<UserSelection>;
}

const mapStateToProps = ({
  domainModels: {userSelections}
}: RootState): StateToProps => ({
  userSelections,
});

export const EditWidgetContainer = connect<StateToProps, {}>(
  mapStateToProps,
)(EditWidget);
