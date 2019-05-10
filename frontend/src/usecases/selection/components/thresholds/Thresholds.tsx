import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {selectedMenuItemStyle} from '../../../../app/themes';
import {ButtonLinkBlue} from '../../../../components/buttons/ButtonLink';
import {DateRange, Period} from '../../../../components/dates/dateModels';
import {PeriodSelection} from '../../../../components/dates/PeriodSelection';
import {TextFieldInput} from '../../../../components/inputs/TextFieldInput';
import {Row, RowBottom, RowMiddle} from '../../../../components/layouts/row/Row';
import {Medium} from '../../../../components/texts/Texts';
import {Maybe} from '../../../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {
  getDisplayModeText,
  Quantity,
  quantityAttributes,
  unitPerHour
} from '../../../../state/ui/graph/measurement/measurementModels';
import {
  defaultDateRange,
  emptyThreshold,
  RelationalOperator,
  ThresholdQuery,
  ThresholdQueryProps
} from '../../../../state/user-selection/userSelectionModels';
import {CallbackWith, ClassNamed, Styled} from '../../../../types/Types';
import {DropDownMenu} from './DropDownMenu';

const dropDownStyle: React.CSSProperties = {
  fontSize: 16,
};

const quantityDropDownStyle: React.CSSProperties = {
  ...dropDownStyle,
  width: 256,
};

const operatorDropDownStyle: React.CSSProperties = {
  ...dropDownStyle,
  width: 100,
};

const valueFieldStyle: React.CSSProperties = {
  width: 114,
  marginBottom: 0,
  marginTop: 8,
};

const makeMenuItem = (text) => makeMenuItemWithValue(text, text);

const makeMenuItemWithValue = (text, value) => <MenuItem key={text} primaryText={text} value={value}/>;

const operatorMenuItems = Object.keys(RelationalOperator)
  .map(key => RelationalOperator[key])
  .map(makeMenuItem);

const quantityMenuItems = Object.keys(Quantity)
  .sort((a, b) => a.localeCompare(b))
  .map(key => Quantity[key])
  .map(makeMenuItem);

interface ThresholdProps {
  query?: ThresholdQuery;
  onChange: CallbackWith<ThresholdQuery>;
}

type Props = ThresholdProps & ClassNamed & Styled;

const thresholdQueryIsModified = (query: ThresholdQueryProps) =>
  query.duration
  || query.quantity
  || query.relationalOperator
  || query.unit
  || query.value !== ''
  || query.dateRange !== defaultDateRange;

const useChangeQuery = (
  initialQuery: ThresholdQueryProps,
  onChange: CallbackWith<ThresholdQuery>
): [ThresholdQueryProps, CallbackWith<ThresholdQueryProps>] => {
  const [value, updateProperty] = React.useState<ThresholdQueryProps>(initialQuery);
  const onChangeThresholdQuery = (query: ThresholdQueryProps) => {
    onChange(query as ThresholdQuery);
    updateProperty(query);
  };
  return [value, onChangeThresholdQuery];
};

export const Thresholds = ({query, onChange, className}: Props) => {
  const [currentQuery, setQuery] = useChangeQuery(query || emptyThreshold, onChange);
  const {quantity, relationalOperator, value, unit, duration, dateRange} = currentQuery;
  const decoratedUnit = unitPerHour(quantity, unit);
  const durationOrNull: string | null = duration || null;
  const onChangeQuantity = (_, __, newValue: string) => setQuery({
    ...currentQuery,
    quantity: newValue as Quantity,
    unit: quantityAttributes[newValue as Quantity].unit
  });

  const onChangeRelationalOperator = (_, __, newValue: string) => setQuery({
    ...currentQuery,
    relationalOperator: newValue as RelationalOperator,
  });

  const onChangeValue = (_, value: string) => setQuery({...currentQuery, value});
  const onChangeDuration = (_, __, duration: string) => setQuery({...currentQuery, duration});
  const selectPeriod = (period: Period) => setQuery({...currentQuery, dateRange: {period}});
  const selectCustomDateRange = (customDateRange: DateRange) => setQuery({
    ...currentQuery,
    dateRange: {period: Period.custom, customDateRange}
  });

  const duringDaysMenuItems = [
    makeMenuItemWithValue(translate('at least once'), null),
    ...[1, 2, 3, 4, 5, 6, 7]
      .map((days: number) =>
        makeMenuItemWithValue(translate('during {{count}} days (or more)', {count: days}), days)
      )
  ];

  const clearThreshold = () => setQuery({...emptyThreshold});

  const clearThresholdButton = thresholdQueryIsModified(currentQuery)
    ? (
      <RowBottom style={{marginLeft: 16, paddingBottom: 12}}>
        <ButtonLinkBlue onClick={clearThreshold}>{translate('clear threshold')}</ButtonLinkBlue>
      </RowBottom>
    )
    : null;

  return (
    <Row className={className}>
      <DropDownMenu
        onChange={onChangeQuantity}
        value={quantity}
        style={quantityDropDownStyle}
        selectedMenuItemStyle={selectedMenuItemStyle}
      >
        {quantityMenuItems}
      </DropDownMenu>

      <DropDownMenu
        onChange={onChangeRelationalOperator}
        value={relationalOperator}
        style={operatorDropDownStyle}
        selectedMenuItemStyle={selectedMenuItemStyle}
      >
        {operatorMenuItems}
      </DropDownMenu>

      <RowMiddle>
        <TextFieldInput
          className="align-right"
          onChange={onChangeValue}
          value={value}
          style={valueFieldStyle}
          autoComplete="off"
          hintText={firstUpperTranslated(getDisplayModeText(quantity))}
        />
        <Medium className="label">{decoratedUnit}</Medium>
      </RowMiddle>

      <RowMiddle>
        <DropDownMenu
          onChange={onChangeDuration}
          value={durationOrNull}
          style={dropDownStyle}
          selectedMenuItemStyle={selectedMenuItemStyle}
        >
          {duringDaysMenuItems}
        </DropDownMenu>
      </RowMiddle>

      <RowMiddle>
        <Medium className="label">{translate('within')}</Medium>
        <PeriodSelection
          customDateRange={dateRange ? Maybe.maybe(dateRange.customDateRange) : Maybe.nothing()}
          period={dateRange ? dateRange.period : Period.latest}
          selectPeriod={selectPeriod}
          setCustomDateRange={selectCustomDateRange}
          style={{marginBottom: 0}}
        />
      </RowMiddle>

      {clearThresholdButton}
    </Row>
  );
};
