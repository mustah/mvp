import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
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
  duringDays,
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
  width: 124
};

const textFieldStyle: React.CSSProperties = {
  width: 114,
  marginBottom: 0,
  marginTop: 8,
};

const makeMenuItem = (text) => (
  <MenuItem key={text} primaryText={text} value={text}/>
);

const makeMenuItemWithValue = (text, value) => (
  <MenuItem key={text} primaryText={text} value={value}/>
);

const operatorMenuItems = Object.keys(RelationalOperator)
  .map((key) => RelationalOperator[key])
  .map(makeMenuItem);

const quantityMenuItems = Object.keys(Quantity)
  .sort((a, b) => a.localeCompare(b))
  .map((key) => Quantity[key])
  .map(makeMenuItem);

const duringDaysMenuItems = [makeMenuItemWithValue('N/A', null)].concat(duringDays.map(makeMenuItem));

interface ThresholdProps {
  query?: ThresholdQuery;
  onChange: OnChangeThreshold;
}

type RenderableThresholdQuery = Partial<{
  [key in keyof ThresholdQuery]: ThresholdQuery[key] | undefined | string
}>;

type Props = ThresholdProps & ClassNamed & Styled;

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

  return (
    <Row className={className}>
      <DropDownMenu
        onChange={onChangeQuantity}
        value={quantity}
        style={quantityDropDownStyle}
      >
        {quantityMenuItems}
      </DropDownMenu>

      <DropDownMenu
        onChange={onChangeRelationalOperator}
        value={relationalOperator}
        style={operatorDropDownStyle}
      >
        {operatorMenuItems}
      </DropDownMenu>

      <RowMiddle>
        <TextFieldInput
          onChange={onChangeValue}
          value={value}
          style={textFieldStyle}
          autoComplete="off"
          hintText={firstUpperTranslated(getDisplayModeText(quantity))}
        />
        <Medium className="Unit">{unit}</Medium>
      </RowMiddle>
      <RowMiddle>
        <Medium className="During">{translate('during')}</Medium>
        <DropDownMenu
          onChange={onChangeDuration}
          value={durationOrNull}
          style={dropDownStyle}
        >
          {duringDaysMenuItems}
        </DropDownMenu>
        <Medium className="Days">{translate('days')}</Medium>
      </RowMiddle>
    </Row>
  );
};
