import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withWidgetLoader} from '../../../components/hoc/withLoaders';
import {Normal} from '../../../components/texts/Texts';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {CountWidget, WidgetMandatory} from '../../../state/domain-models/widget/widgetModels';
import {allCurrentMeterParameters, getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {fetchCountWidget, WidgetRequestParameters} from '../../../state/widget/widgetActions';
import {WidgetState} from '../../../state/widget/widgetReducer';
import {CallbackWith, EncodedUriParameters, OnClick, uuid} from '../../../types/Types';
import {WidgetWithTitle} from '../components/widgets/Widget';

interface OwnProps {
  widget: CountWidget;
  openConfiguration: OnClick;
  onDelete: CallbackWith<WidgetMandatory>;
}

interface StateToProps {
  title: string;
  isUserSelectionsSuccessfullyFetched: boolean;
  parameters: EncodedUriParameters;
  meterCount: number;
}

interface DispatchToProps {
  fetchCountWidget: CallbackWith<WidgetRequestParameters>;
}

type Props = OwnProps & StateToProps & DispatchToProps;

const getMeterCount = (data: WidgetState, id: uuid): number => {
  if (data[id] !== undefined && data[id].data !== undefined) {
    return data[id].data || 0;
  }
  return 0;
};

const mapStateToProps = (
  {domainModels: {userSelections}, widget}: RootState,
  {widget: {settings: {selectionId}, id}}: OwnProps
): StateToProps => {
  const userSelection = selectionId && userSelections.entities[selectionId];

  const parameters = userSelection
    ? getMeterParameters({userSelection})
    : allCurrentMeterParameters;

  const title = userSelection
    ? userSelection.name
    : translate('all meters');

  return {
    isUserSelectionsSuccessfullyFetched: userSelections.isSuccessfullyFetched,
    title,
    parameters,
    meterCount: getMeterCount(widget, id)
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
  widget,
  openConfiguration,
  parameters,
  onDelete,
  fetchCountWidget,
  meterCount
}: Props) => {
  React.useEffect(() => {
    if (isUserSelectionsSuccessfullyFetched) {
      fetchCountWidget({widget, parameters});
    }
  }, [widget, parameters, isUserSelectionsSuccessfullyFetched]);

  const deleteWidget = () => onDelete(widget);
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
