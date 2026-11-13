import { useEffect, useState } from "react";

const emptyForm = {
  firstName: "",
  lastName: "",
  email: "",
  password: "",
  dateOfBirth: "",
  phoneNumber: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  stateProvince: "",
  postalCode: "",
  country: "",
  active: true
};

const profileFields = [
  { name: "firstName", label: "First name", required: true },
  { name: "lastName", label: "Last name", required: true },
  { name: "email", label: "Email address", type: "email", required: true },
  { name: "dateOfBirth", label: "Date of birth", type: "date", required: true },
  { name: "phoneNumber", label: "Phone number", type: "tel" },
  { name: "addressLine1", label: "Address line 1" },
  { name: "addressLine2", label: "Address line 2" },
  { name: "city", label: "City" },
  { name: "stateProvince", label: "State / province" },
  { name: "postalCode", label: "Postal code" },
  { name: "country", label: "Country" }
];

function getProfileData(values) {
  return Object.fromEntries(
    profileFields.map(({ name }) => [name, values[name] || ""])
  );
}

function App() {
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    setLoading(true);
    setError("");

    try {
      const response = await fetch("/api/users");
      if (!response.ok) {
        throw new Error("Could not load users. Please try again.");
      }
      setUsers(await response.json());
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  function handleChange(event) {
    const { name, value, type, checked } = event.target;
    setForm((currentForm) => ({
      ...currentForm,
      [name]: type === "checkbox" ? checked : value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    const userData = {
      ...getProfileData(form),
      ...(editingId ? { active: form.active } : { password: form.password })
    };

    try {
      const response = await fetch(
        editingId ? `/api/users/${editingId}` : "/api/users",
        {
          method: editingId ? "PUT" : "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(userData)
        }
      );

      if (!response.ok) {
        const apiError = await response.json();
        const detail = apiError.details?.join(" ");
        throw new Error(detail || apiError.message || "Could not save this user.");
      }

      setMessage(editingId ? "User updated." : "User registered.");
      resetForm();
      await loadUsers();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  function startEditing(user) {
    setEditingId(user.id);
    setForm({
      ...emptyForm,
      ...user,
      dateOfBirth: user.dateOfBirth || "",
      password: ""
    });
    setMessage("");
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function deleteUser(user) {
    const confirmed = window.confirm(
      `Delete ${user.firstName} ${user.lastName}? This cannot be undone.`
    );
    if (!confirmed) {
      return;
    }

    setError("");
    setMessage("");

    try {
      const response = await fetch(`/api/users/${user.id}`, { method: "DELETE" });
      if (!response.ok) {
        throw new Error("Could not delete this user. Please try again.");
      }
      setMessage("User deleted.");
      if (editingId === user.id) {
        resetForm();
      }
      await loadUsers();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  function resetForm() {
    setForm(emptyForm);
    setEditingId(null);
  }

  return (
    <main className="page">
      <header className="page-header">
        <div>
          <p className="eyebrow">USER DIRECTORY</p>
          <h1>Registration management</h1>
          <p className="subtitle">Create and manage user profile records.</p>
        </div>
        <a className="docs-link" href="/swagger-ui.html" target="_blank" rel="noreferrer">
          API documentation
        </a>
      </header>

      {message && <p className="notice success" role="status">{message}</p>}
      {error && <p className="notice error" role="alert">{error}</p>}

      <section className="panel">
        <div className="section-heading">
          <div>
            <p className="eyebrow">{editingId ? "EDIT PROFILE" : "NEW PROFILE"}</p>
            <h2>{editingId ? "Update user" : "Register a user"}</h2>
          </div>
          {editingId && (
            <button className="button secondary" type="button" onClick={resetForm}>
              Cancel editing
            </button>
          )}
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            {profileFields.map((field) => (
              <label className="field" key={field.name}>
                <span>{field.label}{field.required && <strong> *</strong>}</span>
                <input
                  name={field.name}
                  type={field.type || "text"}
                  value={form[field.name] || ""}
                  onChange={handleChange}
                  required={field.required}
                  maxLength={field.name === "email" ? 254 : undefined}
                />
              </label>
            ))}

            {!editingId && (
              <label className="field">
                <span>Password <strong>*</strong></span>
                <input
                  name="password"
                  type="password"
                  value={form.password}
                  onChange={handleChange}
                  minLength="8"
                  maxLength="72"
                  autoComplete="new-password"
                  required
                />
                <small>At least 8 characters. The API stores only a secure hash.</small>
              </label>
            )}

            {editingId && (
              <label className="checkbox-field">
                <input
                  name="active"
                  type="checkbox"
                  checked={form.active}
                  onChange={handleChange}
                />
                <span>Account is active</span>
              </label>
            )}
          </div>

          <div className="form-actions">
            <button className="button primary" type="submit">
              {editingId ? "Save changes" : "Register user"}
            </button>
            {!editingId && (
              <button className="button secondary" type="button" onClick={resetForm}>
                Clear form
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="panel">
        <div className="section-heading">
          <div>
            <p className="eyebrow">SAVED RECORDS</p>
            <h2>Users <span className="count">{users.length}</span></h2>
          </div>
          <button className="button secondary" type="button" onClick={loadUsers}>
            Refresh
          </button>
        </div>

        {loading ? (
          <p className="empty-state">Loading users...</p>
        ) : users.length === 0 ? (
          <p className="empty-state">No users yet. Register the first user above.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Date of birth</th>
                  <th>Location</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.firstName} {user.lastName}</td>
                    <td>{user.email}</td>
                    <td>{user.dateOfBirth}</td>
                    <td>{[user.city, user.country].filter(Boolean).join(", ") || "—"}</td>
                    <td>
                      <span className={user.active ? "status active" : "status inactive"}>
                        {user.active ? "Active" : "Inactive"}
                      </span>
                    </td>
                    <td className="row-actions">
                      <button
                        className="text-button"
                        type="button"
                        onClick={() => startEditing(user)}
                      >
                        Edit
                      </button>
                      <button
                        className="text-button delete"
                        type="button"
                        onClick={() => deleteUser(user)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}

export default App;
