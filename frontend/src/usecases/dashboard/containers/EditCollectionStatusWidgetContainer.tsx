import * as React from 'react';
import {connect} from 'react-redux';
import {ButtonCancel, ButtonConfirm} from '../../../components/buttons/DialogButtons';
import {PeriodSelection} from '../../../components/dates/PeriodSelection';
import {Dialog} from '../../../components/dialog/Dialog';
import {SelectFieldInput} from '../../../components/inputs/InputSelectable';
import {MainTitle} from '../../../components/texts/Titles';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {WidgetType} from '../../../state/widget/configuration/widgetConfigurationReducer';
import {CallbackWith, IdNamed, OnClick, uuid} from '../../../types/Types';
import {CollectionStatusWidgetSettings} from './CollectionStatusContainer';
import '../components/widgets/EditWidget.scss'

const noop = () => null;

const ALL_METERS = -1;

const EditCollectionStatusWidget = ({
  userSelections,
  isOpen,
  onCancel,
  onSave,
  id,
  dashboardId,
  settings: {settings},
}: EditCollectionStatusWidgetProps) => {

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

  const selectedSelection: uuid = selectionId || selectionOptions[0].id;

  const save = () => {
    const widget: CollectionStatusWidgetSettings = {
      id,
      settings: {
        selectionInterval: settings.selectionInterval,
      },
      type: WidgetType.COLLECTION,
      dashboardId,
    };

    if (selectionId !== ALL_METERS) {
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
      <PeriodSelection
        customDateRange={Maybe.nothing()}
        period={settings.selectionInterval.period}
        selectPeriod={noop}
        setCustomDateRange={noop}
        style={{marginBottom: 0}}
        disabled={true}
      />
    </Dialog>
  );
};

type EditCollectionStatusWidgetProps = StateToProps & OwnProps;

interface OwnProps {
  isOpen: boolean;
  onCancel: OnClick;
  onSave: CallbackWith<CollectionStatusWidgetSettings>;
  id: uuid;
  dashboardId: uuid;
  settings: CollectionStatusWidgetSettings;
}

interface StateToProps {
  userSelections: NormalizedState<UserSelection>;
}

const mapStateToProps = ({
  domainModels: {userSelections}
}: RootState): StateToProps => ({
  userSelections,
});

export const EditCollectionStatusWidgetContainer = connect<StateToProps, {}>(
  mapStateToProps
)(EditCollectionStatusWidget);
