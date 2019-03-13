import * as React from 'react';
import {connect} from 'react-redux';
import {ButtonCancel, ButtonConfirm} from '../../../../components/buttons/DialogButtons';
import {Dialog} from '../../../../components/dialog/Dialog';
import {SelectFieldInput} from '../../../../components/inputs/InputSelectable';
import {Subtitle} from '../../../../components/texts/Titles';
import {RootState} from '../../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../../services/translationService';
import {NormalizedState} from '../../../../state/domain-models/domainModels';
import {UserSelection} from '../../../../state/user-selection/userSelectionModels';
import {WidgetType} from '../../../../state/widget/configuration/widgetConfigurationReducer';
import {CallbackWith, IdNamed, OnClick, uuid} from '../../../../types/Types';
import {MapWidgetSettings} from '../../containers/MapWidgetContainer';

const ALL_METERS = -1;

const EditMapWidget = (props: Props) => {
  const {
    userSelections,
    isOpen,
    onCancel,
    onSave,
    id,
    dashboardId,
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
    const widget: MapWidgetSettings = {
      id,
      settings: {},
      type: WidgetType.MAP,
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
    >
      <Subtitle>{firstUpperTranslated('widget settings')}</Subtitle>
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

type Props = StateToProps & OwnProps;

interface OwnProps {
  isOpen: boolean;
  onCancel: OnClick;
  onSave: CallbackWith<MapWidgetSettings>;
  id: uuid;
  dashboardId: uuid;
  settings: MapWidgetSettings;
}

interface StateToProps {
  userSelections: NormalizedState<UserSelection>;
}

const mapStateToProps = ({
  domainModels: {userSelections}
}: RootState): StateToProps => ({
  userSelections,
});

export const EditMapWidgetContainer = connect<StateToProps, {}>(
  mapStateToProps,
)(EditMapWidget);
