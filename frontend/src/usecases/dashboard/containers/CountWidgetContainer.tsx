import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withWidgetLoader} from '../../../components/hoc/withLoaders';
import {Normal} from '../../../components/texts/Texts';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {allCurrentMeterParameters, getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {WidgetMandatory, WidgetType} from '../../../state/widget/configuration/widgetConfigurationReducer';
import {fetchCountWidget, FetchWidgetIfNeeded} from '../../../state/widget/data/widgetDataActions';
import {CallbackWith, EncodedUriParameters, OnClick, uuid} from '../../../types/Types';
import {WidgetWithTitle} from '../components/widgets/Widget';

export interface CountWidgetSettings extends WidgetMandatory {
  type: WidgetType.COUNT;
  settings: {
    selectionId?: uuid;
  };
}

interface OwnProps {
  settings: CountWidgetSettings;
  openConfiguration: OnClick;
  onDelete: CallbackWith<WidgetMandatory>;
}

interface StateToProps {
  title: string;
  isUserSelectionsSuccessfullyFetched: boolean;
  parameters: EncodedUriParameters;
  meterCount?: number;
}

export interface DispatchToProps {
  fetchCountWidget: CallbackWith<FetchWidgetIfNeeded>;
}

type Props = OwnProps & StateToProps & DispatchToProps;

const mapStateToProps = (
  {domainModels: {userSelections}, widget: {data}}: RootState,
  {settings: {settings: {selectionId}, id}}: OwnProps
): StateToProps => {
  const userSelection = selectionId && userSelections.entities[selectionId];

  const parameters = userSelection
    ? getMeterParameters({userSelection})
    : allCurrentMeterParameters;

  const title = userSelection
    ? userSelection.name
    : translate('all meters');

  let meterCount;
  if (data[id] !== undefined && data[id].data !== undefined) {
    meterCount = data[id].data;
  }
  return {
    isUserSelectionsSuccessfullyFetched: userSelections.isSuccessfullyFetched,
    title,
    parameters,
    meterCount
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchCountWidget,
}, dispatch);

interface CountContentProps {
  meterCount: number;
}

const CountContent = ({meterCount}: CountContentProps) => <Normal>{meterCount}</Normal>;

const CountContentWidgetLoader = withWidgetLoader<CountContentProps>(CountContent);
const CountWidget = ({
  isUserSelectionsSuccessfullyFetched,
  title,
  settings,
  openConfiguration,
  parameters,
  onDelete,
  fetchCountWidget,
  meterCount
}: Props) => {
  React.useEffect(() => {
    if (isUserSelectionsSuccessfullyFetched) {
      fetchCountWidget({settings, parameters});
    }
  }, [settings, parameters, isUserSelectionsSuccessfullyFetched]);

  const deleteWidget = () => onDelete(settings);
  return (
    <WidgetWithTitle
      title={title}
      className="CountWidget"
      configure={openConfiguration}
      deleteWidget={deleteWidget}
    >
      <CountContentWidgetLoader
        meterCount={meterCount!}
        isFetching={!isUserSelectionsSuccessfullyFetched || meterCount === undefined}
      />
    </WidgetWithTitle>
  );
};

export const CountWidgetContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CountWidget);
