import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {selectedStyle} from '../../../../app/themes';
import {ButtonLinkBlue} from '../../../../components/buttons/ButtonLink';
import {TextFieldInput} from '../../../../components/inputs/TextFieldInput';
import {Row, RowMiddle} from '../../../../components/layouts/row/Row';
import {Medium} from '../../../../components/texts/Texts';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {
  getDisplayModeText,
  Quantity,
  quantityAttributes
} from '../../../../state/ui/graph/measurement/measurementModels';
import {
  OnChangeThreshold,
  RelationalOperator,
  ThresholdQuery
} from '../../../../state/user-selection/userSelectionModels';
import {CallbackWith, ClassNamed, Styled} from '../../../../types/Types';
import '../SelectionResultList.scss';
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
  .map((key) => RelationalOperator[key])
  .map(makeMenuItem);

const quantityMenuItems = Object.keys(Quantity)
  .sort((a, b) => a.localeCompare(b))
  .map((key) => Quantity[key])
  .map(makeMenuItem);

interface ThresholdProps {
  query?: ThresholdQuery;
  onChange: OnChangeThreshold;
}

type RenderableThresholdQuery = Partial<{
  [key in keyof ThresholdQuery]: ThresholdQuery[key] | undefined | string
}>;

type Props = ThresholdProps & ClassNamed & Styled;

const thresholdQueryIsModified = (query: RenderableThresholdQuery) =>
  query.duration || query.quantity || query.relationalOperator || query.unit || query.value !== '';

const useChangeQuery = (
  initialQuery: RenderableThresholdQuery,
  onChange: OnChangeThreshold
): [RenderableThresholdQuery, CallbackWith<RenderableThresholdQuery>] => {
  const [value, updateProperty] = React.useState<RenderableThresholdQuery>(initialQuery);
  const onChangeThresholdQuery = (query: RenderableThresholdQuery) => {
    onChange(query as ThresholdQuery);
    updateProperty(query);
  };
  return [value, onChangeThresholdQuery];
};

const emptyQuery: RenderableThresholdQuery = {
  value: '',
};

export const Thresholds = ({query = emptyQuery, onChange, className}: Props) => {
  const [currentQuery, setQuery] = useChangeQuery(query, onChange);
  const {quantity, relationalOperator, value, unit, duration} = currentQuery;
  const durationOrNull = !duration ? null : duration;
  const onChangeQuantity = (event, index, newValue: string) => setQuery({
    ...currentQuery,
    quantity: newValue as Quantity,
    unit: quantityAttributes[newValue as Quantity].unit
  });

  const onChangeRelationalOperator = (event, index, newValue: string) => setQuery({
    ...currentQuery,
    relationalOperator: newValue as RelationalOperator,
  });

  const onChangeValue = (event, value: string) => setQuery({...currentQuery, value});
  const onChangeDuration = (event, index, duration: string) => setQuery({...currentQuery, duration});

  const duringDaysMenuItems = [
    makeMenuItemWithValue(translate('at least once'), null),
    ...[1, 2, 3, 4, 5, 6, 7]
      .map((days: number) =>
        makeMenuItemWithValue(translate('during {{count}} days (or more)', {count: days}), days)
      )
  ];

  const clearThreshold = () => setQuery({...emptyQuery});

  const clearThresholdButton = thresholdQueryIsModified(currentQuery)
    ? (
      <RowMiddle>
        <ButtonLinkBlue onClick={clearThreshold}>{translate('clear threshold')}</ButtonLinkBlue>
      </RowMiddle>
    )
    : null;

  return (
    <Row className={className}>
      <DropDownMenu
        onChange={onChangeQuantity}
        value={quantity}
        style={quantityDropDownStyle}
        selectedMenuItemStyle={selectedStyle}
      >
        {quantityMenuItems}
      </DropDownMenu>

      <DropDownMenu
        onChange={onChangeRelationalOperator}
        value={relationalOperator}
        style={operatorDropDownStyle}
        selectedMenuItemStyle={selectedStyle}
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
        <Medium className="Unit">{unit}</Medium>
      </RowMiddle>

      <RowMiddle>
        <DropDownMenu
          onChange={onChangeDuration}
          value={durationOrNull}
          style={dropDownStyle}
          selectedMenuItemStyle={selectedStyle}
        >
          {duringDaysMenuItems}
        </DropDownMenu>
      </RowMiddle>

      {clearThresholdButton}
    </Row>
  );
};
