import * as React from 'react';
import {connect} from 'react-redux';
import {ButtonCancel, ButtonConfirm} from '../../../components/buttons/DialogButtons';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {PeriodSelection} from '../../../components/dates/PeriodSelection';
import {Dialog} from '../../../components/dialog/Dialog';
import {SelectFieldInput} from '../../../components/inputs/InputSelectable';
import {MainTitle} from '../../../components/texts/Titles';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {CollectionStatusWidget, WidgetType} from '../../../state/domain-models/widget/widgetModels';
import {initialSelectionId, UserSelection} from '../../../state/user-selection/userSelectionModels';
import {CallbackWith, IdNamed, OnClick, uuid} from '../../../types/Types';
import '../components/EditWidget.scss';

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
    {id: initialSelectionId, name: firstUpperTranslated('all meters')},
    ...userSelections.result.map(
      (id: uuid) => ({
        id,
        name: userSelections.entities[id].name,
      })
    )
  ];
  const selectionLabel = firstUpperTranslated('selection');
  const [selectionId, selectSelection] = React.useState(settings.selectionId);
  const [selectionInterval, selectSelectionInterval] = React.useState(settings.selectionInterval);

  const selectPeriod = (period: Period) => selectSelectionInterval({period});
  const setCustomDateRange = (customDateRange: DateRange) => selectSelectionInterval({
    period: Period.custom,
    customDateRange
  });

  const onChange = (event, index, value) => {
    selectSelection(value);
  };

  const selectedSelection: uuid = selectionId || selectionOptions[0].id;

  const save = () => {
    const widget: CollectionStatusWidget = {
      id,
      settings: {
        selectionInterval,
      },
      type: WidgetType.COLLECTION,
      dashboardId,
    };

    if (selectionId !== initialSelectionId) {
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
        customDateRange={Maybe.maybe(selectionInterval.customDateRange)}
        period={selectionInterval.period}
        selectPeriod={selectPeriod}
        setCustomDateRange={setCustomDateRange}
        style={{marginLeft: 0, marginBottom: 0}}
      />
    </Dialog>
  );
};

type EditCollectionStatusWidgetProps = StateToProps & OwnProps;

interface OwnProps {
  isOpen: boolean;
  onCancel: OnClick;
  onSave: CallbackWith<CollectionStatusWidget>;
  id: uuid;
  dashboardId: uuid;
  settings: CollectionStatusWidget;
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
