const sum = (a: number, b: number) => a + b;

it('adds 1 + 2 to equal 3 in TScript', () => {
  expect(sum(1, 2)).toBe(3);
});

it('adds 1 + 2 to equal 3 in JavaScript', () => {
  expect(sum(1, 2)).toBe(3);
});
